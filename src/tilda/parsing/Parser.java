/* ===========================================================================
 * Copyright (C) 2015 CapsicoHealth Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tilda.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tilda.generation.interfaces.CodeGenSql;
import tilda.parsing.parts.Schema;
import tilda.utils.FileUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class Parser
  {
    protected static final Logger LOG = LogManager.getLogger(Parser.class.getName());

    public static ParserSession parse(String FilePath, CodeGenSql CGSql)
      {
        LOG.info("\n\n\n-----------------------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("Loading Tilda schema '" + FilePath + "'.");
        Schema S = fromFile(FilePath);
        if (S == null)
          return null;

        ParserSession PS = new ParserSession(S, CGSql);
        if (loadDependencies(PS, S) == false)
          return null;

        if (PS.getErrorCount() != 0)
          {
            LOG.error("==============================================================================================");
            LOG.error("There were " + PS.getErrorCount() + " errors when trying to validate the schema set");
            int i = 0;
            for (String Err : PS._Errors)
              LOG.error("    " + (++i) + " - " + Err);
            return null;
          }

        return PS;
      }

    protected static Schema fromFile(String FilePath)
      {
        Reader R = null;
        try
          {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            R = new FileReader(FilePath);
            Schema S = gson.fromJson(R, tilda.parsing.parts.Schema.class);
            S.setOrigin(FilePath);
            return S;
          }
        catch (Throwable T)
          {
            LOG.error("Cannot load Tilda schema from file '" + FilePath + "'.", T);
            return null;
          }
        finally
          {
            if (R != null)
              try
                {
                  R.close();
                }
              catch (IOException e)
                {
                }
          }
      }

    protected static Schema fromResource(String ResourceName)
      {
        Reader R = null;
        try
          {
            InputStream In = FileUtil.getResourceAsStream(ResourceName);
            if (In == null)
              {
                LOG.error("Cannot find Tilda resource '" + ResourceName + "'.");
                return null;
              }
            R = new BufferedReader(new InputStreamReader(In));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Schema S = gson.fromJson(R, tilda.parsing.parts.Schema.class);
            S.setOrigin(ResourceName);
            return S;
          }
        catch (Throwable T)
          {
            LOG.error("Cannot load Tilda schema from resource '" + ResourceName + "'.", T);
            return null;
          }
        finally
          {
            if (R != null)
              try
                {
                  R.close();
                }
              catch (IOException e)
                {
                }
          }
      }

    public static boolean loadDependencies(ParserSession PS, Schema BaseSchema)
      {
        String BaseTildaSchemaResource = "tilda/data/_tilda.Tilda.json";
        Schema BaseTilda = null;
        if (BaseSchema._ResourceName.endsWith(BaseTildaSchemaResource) == false)
          {
            LOG.info("Loading base Tilda schema from '" + BaseTildaSchemaResource + "'.");
            BaseTilda = fromResource(BaseTildaSchemaResource);
            if (BaseTilda == null)
              return false;
            PS._Dependencies.put(BaseTilda.getFullName(), BaseTilda);
            if (loadDependencies(BaseSchema, PS._Dependencies) == false)
              return false;
          }
        else
          BaseTilda = BaseSchema;

        // We need to reorder the list of dependent schemas for validation to work properly, i.e.,
        // if schema A depends on Schema B, then B should be before in the list.
        List<Schema> Schemas = new ArrayList<Schema>(PS._Dependencies.values());
        Schema S1 = null;
        Schema S2 = null;
        for (int i = 0; i < Schemas.size(); ++i)
          {
            Schema s = Schemas.get(i);
            if (s == null)
              continue;
            // Let's find the index the dependency schema that's highest in the list.
            int highestDependentindex = -1;
            if (s._DependencySchemas.isEmpty() == false)
              for (Schema d : s._DependencySchemas)
                {
                  int k = Schemas.indexOf(d);
                  if (k > highestDependentindex)
                    highestDependentindex = k;
                }
            // May repeat if the base Tilda schema was explicitly defined as a dependency, but this is
            // such a small operation, it's not worth optimizing.
            int k = Schemas.indexOf(BaseTilda);
            if (k > highestDependentindex)
              highestDependentindex = k;

            // If the highest index is greater than that of the current schema, we need to swap.
            if (highestDependentindex > i)
              {
                Schema NewS1 = Schemas.get(highestDependentindex);
                if (S1 != null) // no check for the first loop.
                  {
                    // Check if there is a circular dependency!
                    if (S1.getFullName().equals(NewS1.getFullName()) && S2.getFullName().equals(s.getFullName())
                        || S1.getFullName().equals(s.getFullName()) && S2.getFullName().equals(NewS1.getFullName()))
                      {
                        PS.AddError("There is a circular dependency between schemas '" + S1.getFullName() + "' and '" + S2.getFullName() + "'.");
                        return false;
                      }
                  }
                // Update the running swapping state
                S1 = NewS1;
                S2 = s;
                // Do the swapping
                Schemas.set(i, S1);
                Schemas.set(highestDependentindex, S2);
                // Go back one in the index to make sure we re-process the element again that was just swapped.
                // By definition, it was after the current schema in the loop and therefore wasn't processed yet.
                --i;
              }
          }

        LOG.debug("  Reordered dependencies:");
        Iterator<Schema> I = Schemas.iterator();
        while (I.hasNext() == true)
          LOG.debug("    - " + I.next().getFullName() + ".");

        I = Schemas.iterator();
        while (I.hasNext() == true)
          if (I.next().Validate(PS) == false)
            break;
        if (PS.getErrorCount() == 0)
          {
            if (PS.getSchema(BaseSchema._Package, BaseSchema._Name) == null)
              {
                PS._Dependencies.put(BaseSchema.getFullName(), BaseSchema);
              }
            BaseSchema.Validate(PS);
          }

        return true;
      }

    private static boolean loadDependencies(Schema S, Map<String, Schema> Dependencies)
      {
        if (S._Dependencies != null)
          for (String d : S._Dependencies)
            {
              LOG.info("Loading dependency schema from '" + d + "'.");
              Schema D = fromResource(d);
              if (D == null)
                return false;
              Schema Pre = Dependencies.get(D.getFullName());
              if (Pre != null)
                {
                  LOG.info("   Tilda dependency schema '" + Pre.getFullName() + "' has been loaded already");
                  S._DependencySchemas.add(Pre);
                }
              else
                {
                  S._DependencySchemas.add(D);
                  Dependencies.put(D.getFullName(), D);
                  if (loadDependencies(D, Dependencies) == false)
                    return false;
                }
            }
        return true;
      }

  }
