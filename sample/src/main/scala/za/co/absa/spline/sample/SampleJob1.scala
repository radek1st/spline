/*
 * Copyright 2017 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.sample

import org.apache.spark.sql.{SaveMode, SparkSession}
import za.co.absa.spline.core.SparkLineageInitializer._

object SampleJob1 {
  def main(args: Array[String]) {
    val sparkBuilder = SparkSession.builder().appName("Sample Job 1")
    val spark = sparkBuilder.getOrCreate()

    // Initializing library to hook up to Apache Spark
    spark.enableLineageTracking()

    // A business logic of a spark job ...
    import spark.implicits._
    val sourceDS = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("data/input/wikidata.csv")
      .as("source")
      .filter($"total_response_size" > 1000)
      .filter($"count_views" > 10)

    val domainMappingDS = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("data/input/domain.csv")
      .as("mapping")

    val joinedDS = sourceDS
      .join(domainMappingDS, $"domain_code" === $"d_code", "left_outer")
      .select($"page_title".as("page"), $"d_name".as("domain"), $"count_views")

    joinedDS.write.mode(SaveMode.Overwrite).parquet("data/results/job1_results")
  }
}
