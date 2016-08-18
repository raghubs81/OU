package com.maga.ou.model.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.maga.ou.util.UIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Raghu on 22-Jul-2016.
 */
public class DBQueryBuilder
{
   public final String TAG = "ou." + getClass().getSimpleName();

   private SQLiteDatabase db;

   private List<String> listTable = new ArrayList<>();

   private List<String> listColumn = new ArrayList<>();

   private List<String> listOrderBy = new ArrayList<>();

   private List<String> listGroupBy = new ArrayList<>();

   private StringBuilder builderWhereTemplate = new StringBuilder ();

   private String whereValue[] = null;

   private String having = null;

   private String limit = null;

   private boolean distinct;

   public enum Order
   {
      ASC, DESC;
   }

   public DBQueryBuilder (SQLiteDatabase db)
   {
      this.db = db;
   }

   public DBQueryBuilder select (AbstractColumn... column)
   {
      validateNonEmpty(column);
      for (AbstractColumn curr : column)
         listColumn.add(curr.toString());
      return this;
   }

   public DBQueryBuilder selectRaw (String... token)
   {
      validateNonEmpty(token);
      for (String curr : token)
         listColumn.add(curr);
      return this;
   }

   public DBQueryBuilder from (AbstractTable... table)
   {
      validateNonEmpty(table);
      for (AbstractTable curr : table)
         listTable.add(curr.toString());
      return this;
   }

   public DBQueryBuilder where (String... where)
   {
      for (String curr : where)
         builderWhereTemplate.append(curr);
      builderWhereTemplate.append(" ");
      return this;
   }

   public DBQueryBuilder whereAND (String... where)
   {
      builderWhereTemplate.append (TextUtils.join(" AND ", where));
      builderWhereTemplate.append(" ");
      return this;
   }

   public DBQueryBuilder whereOR (String... where)
   {
      builderWhereTemplate.append(TextUtils.join(" OR ", where));
      builderWhereTemplate.append(" ");
      return this;
   }

   public DBQueryBuilder whereValue (String... where)
   {
      this.whereValue = where;
      return this;
   }

   public DBQueryBuilder orderBy (AbstractColumn... column)
   {
      return orderBy (column, null);
   }

   public DBQueryBuilder orderByDesc (AbstractColumn... column)
   {
      Order order[] = new Order[column.length];
      Arrays.fill(order, Order.DESC);
      return orderBy(column, order);
   }

   public DBQueryBuilder orderBy (AbstractColumn column[], Order order[])
   {
      validateNonEmpty(column);
      if (order != null)
         DBUtil.assertEquals(order.length, column.length, "Count of columns and order must be the same.");

      for (int i = 0; i < column.length; ++i)
      {
         if (order == null)
            listOrderBy.add(column[i].toString());
         else
            listOrderBy.add(column[i] + " " + order[i]);
      }
      return this;
   }

   public DBQueryBuilder groupBy (AbstractColumn... column)
   {
      validateNonEmpty(column);
      for (AbstractColumn curr : column)
         listGroupBy.add(curr.toString());
      return this;
   }

   public DBQueryBuilder having (String having)
   {
      this.having = having;
      return this;
   }

   public DBQueryBuilder limit (String limit)
   {
      this.limit = limit;
      return this;
   }

   public DBQueryBuilder distinct (boolean value)
   {
      this.distinct = value;
      return this;
   }


   public Cursor query ()
   {
      if (listTable.isEmpty())
         DBUtil.die("No table selected");

      String tokenTable    = TextUtils.join(",", listTable);
      String tokenColumn[] = listColumn.isEmpty()  ? null : listColumn.toArray(new String[0]);
      String tokenWhere    = builderWhereTemplate.toString().trim().equals("") ? null : builderWhereTemplate.toString().trim();
      String tokenOrderBy  = listOrderBy.isEmpty() ? null : TextUtils.join(",", listOrderBy);
      String tokenGroupBy  = listGroupBy.isEmpty() ? null : TextUtils.join(",", listGroupBy);

      StringBuilder builder = new StringBuilder ();
      builder.append ("SELECT ").append((distinct) ? "DISTINCT ": "")
             .append((tokenColumn == null) ? "* " : TextUtils.join(", ", tokenColumn)).append (" ")
             .append("FROM ").append(TextUtils.join(", ", listTable)).append(" ");

      if (tokenWhere != null)
         builder.append("WHERE ").append(tokenWhere).append (" ");

      if (whereValue != null)
         builder.append ("WHERE-VALUE ").append(TextUtils.join(", ", whereValue)).append (" ");

      if (tokenGroupBy != null)
         builder.append("GROUP BY ").append(tokenGroupBy).append(" ");

      if (tokenOrderBy != null)
         builder.append("ORDER BY ").append(tokenOrderBy).append(" ");

      if (having != null)
         builder.append("HAVING ").append(having).append(" ");

      if (limit != null)
         builder.append ("LIMIT " + limit);

      Log.d(TAG, builder.toString());

      return db.query(distinct, tokenTable, tokenColumn, tokenWhere, whereValue, tokenGroupBy, having, tokenOrderBy, limit);
   }

   private void validateNonEmpty(Object obj[])
   {
      if (obj == null || obj.length == 0)
         DBUtil.die("At least one entry is required");
   }
}
