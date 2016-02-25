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

package tilda.db.stores;

import java.sql.Array;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tilda.data.ZoneInfo_Data;
import tilda.db.Connection;
import tilda.enums.AggregateType;
import tilda.enums.ColumnType;
import tilda.generation.interfaces.CodeGenSql;
import tilda.parsing.parts.Column;
import tilda.parsing.parts.Object;
import tilda.parsing.parts.Schema;
import tilda.parsing.parts.View;
import tilda.utils.TextUtil;

public class MSSQL implements DBType
  {
    static final Logger LOG = LogManager.getLogger(MSSQL.class.getName());
    
    @Override
    public boolean isErrNoData(String SQLState, int ErrorCode)
      {
        return SQLState.equals("23000") || ErrorCode == 2601;
      }


    @Override
    public String getCurrentTimestampStr()
      {
        return "current_timestamp";
      }


    @Override
    public String getName()
      {
        return "SQLServer";
      }

    protected static final String[] _LOCK_CONN_ERROR_SUBSTR = { "deadlocked on lock"
        , "lock request time out"
        , "lock inconsistency found"
        , "connection reset"
        , "connection is closed"
        };

    @Override
    public boolean isLockOrConnectionError(SQLException E)
      {
        return TextUtil.indexOf(E.getMessage().toLowerCase(), _LOCK_CONN_ERROR_SUBSTR);
      }
    
    @Override
    public boolean needsSavepoint()
      {
        return false;
      }
    
    @Override
    public boolean supportsSelectLimit()
      {
        return false;
      }

    @Override
    public boolean supportsSelectOffset()
      {
        return false;
      }

    @Override
    public String getSelectLimitClause(int Start, int Size)
      {
        return "";
      }

    @Override
    public boolean FullIdentifierOnUpdate()
      {
        return false;
      }
    
    @Override
    public String getAggregateStr(AggregateType AT)
      {
        switch (AT)
          {
            case AVG:
              return "avg";
            case DEV:
              return "stddev";
            case MAX:
              return "max";
            case MIN:
              return "min";
            case SUM:
              return "sum";
            case VAR:
              return "var";
            default:
              throw new Error("Cannot convert AggregateType " + AT + " to a database aggregate function name.");
          }
      }

    @Override
    public boolean alterTableAddColumn(Connection Con, Column Col, String DefaultValue) throws Exception
      {
        throw new UnsupportedOperationException();
      }

    @Override
    public CodeGenSql getSQlCodeGen()
      {
        throw new UnsupportedOperationException();
      }

    @Override
    public boolean createTable(Connection Con, Object Obj)
      throws Exception
      {
        throw new UnsupportedOperationException();
      }
    
    @Override
    public boolean createView(Connection Con, View V, boolean Drop)
      throws Exception
      {
        throw new UnsupportedOperationException();
      }
    
    @Override
    public boolean createSchema(Connection Con, Schema S)
      throws Exception
      {
        throw new UnsupportedOperationException();
      }

    @Override
    public boolean alterTableAlterColumnNull(Connection Con, Column Col, String DefaultValue)
      throws Exception
      {
        throw new UnsupportedOperationException();
      }
    
    @Override
    public int getCLOBThreshhold()
      {
        return 4096;
      }
    
    @Override
    public boolean alterTableAlterColumnStringSize(Connection Con, Column Col, int DBSize)
    throws Exception
      {
        throw new UnsupportedOperationException();
      }
    
    @Override
    public boolean alterTableAlterColumnType(Connection Con, ColumnType fromType, Column Col, ZoneInfo_Data defaultZI)
      {
        throw new UnsupportedOperationException();
      }
    
    @Override
    public boolean addHelperFunctions(Connection Con) throws Exception
     {
       throw new UnsupportedOperationException();
     }    
    
    @Override
    public Array createArrayOf(Connection Con, ColumnType Type, java.lang.Object[] A)
    throws SQLException
      {
        throw new UnsupportedOperationException();
      }
  }
