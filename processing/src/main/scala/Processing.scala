package com.cognira.Challenge

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.SparkConf
import org.apache.log4j.Level
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructType}

object Processing
{
    def main(args: Array[String]): Unit=
    {   
        // creating the spark configuration
        val conf: SparkConf = new SparkConf(true)
            .setAppName("challenge")
            .setMaster("local[*]")
            .set("spark.cassandra.connection.host", "cassandra")
            .set("spark.cassandra.connection.port", "9042")
            //.set("spark.cassandra.auth.username", sys.env.getOrElse("DB_USER", "cassandra"))
            //.set("spark.cassandra.auth.password", sys.env.getOrElse("DB_PASS", "cassandra"))
        
        // creating the spark session
        val spark =  SparkSession
            .builder()
            .config(conf)
            .getOrCreate()
        
        // limiting the logs to WARN and ERROR
        spark.sparkContext.setLogLevel("WARN")

        // extracting the transaction logs data from the CSV files
        val newSchema = new StructType()
                            .add("customer_id", IntegerType, nullable = true)
                            .add("purch_week", IntegerType, nullable = true)
                            .add("prod_purch", IntegerType, nullable = true)
                            .add("promo_cat", IntegerType, nullable = true)
                            .add("promo_discount", DoubleType, nullable = true)
                            .add("store_id", IntegerType, nullable = true)

        val df = spark.read
                      .format("csv")
                      .option("inferSchema", "true")
                      .schema(newSchema)
                      .load("/data/datagen_*.csv")

        val t_data = df.withColumn("units_sold",lit(1))

        // getting the weekly sales for each (product, store, week, promotion)
        val weekly_sales_df = t_data.groupBy("purch_week","sku","store_id","promo_cat","promo_disc")
                                .agg(sum("units_sold").alias("weekly_sales"))

        // metrics calculation

        // calculating the baseline for each (product, store) without promotion
        val baseline_df = weekly_sales_df.filter(col("promo_cat") === "nan")
                                         .groupBy("sku","store_id")
                                         .agg(expr("percentile(weekly_sales, 0.5)").as("baseline"))

        // calculating the lift unit and percentage for each (product, store) that have a promotion
        val lift = weekly_sales_df.join(baseline_df,Seq("sku","store_id"), "left_outer")
                                  .withColumn("lift_unit", col("weekly_sales") - col("baseline"))
                                  .withColumn("lift_percentage", when(col("promo_cat") !== "nan", col("weekly_sales").divide(col("baseline"))).otherwise(1.0))

        // calculating the total sales for each (product, promotion)
        val total_sales = weekly_sales_df.groupBy("sku","promo_cat","promo_disc")
                            .agg(sum("weekly_sales").alias("total_sales_promo_prod"))

        // building the final "weekly sales dataframe" to load into the cassandra database 
        val final_df = lift.join(total_sales, Seq("sku","promo_cat","promo_disc"), "left_outer")
        final_df.printSchema()
        //final_df.show()
        
        println("*** WRITING TO CASSANDRA TO TABLE db_sales IN KEYSPACE sales ***")
        final_df.write
                .format("org.apache.spark.sql.cassandra")
                .options(Map(
                    "keyspace" -> "sales",
                    "table" -> "db_sales"))
                .mode("append")
                .save()

        println("*** READING FROM CASSANDRA FROM TABLE db_sales IN KEYSPACE sales ***")
        val loaded_data = spark.read
                         .format("org.apache.spark.sql.cassandra")
                         .options(Map(
                             "keyspace" -> "sales",
                             "table" -> "db_sales"))
                         .load()
        loaded_data.show()

        println("*** STOPPING SPARK SESSION ***")
        spark.stop()
    }
}
