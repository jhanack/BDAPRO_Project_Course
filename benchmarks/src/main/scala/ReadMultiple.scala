import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.collection.JavaConverters._

object ReadMultiple {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder
      .config("spark.jars", "src/main/JDBC_Driver/postgresql-42.3.2.jar")
      .config("spark.master", "local")
      .appName("JDBC Read Multiple")
      .getOrCreate()


//    import spark.implicits._

    import java.util.Properties

    var properties: Properties = null
    val url = getClass.getResource("/td.properties")
    if (url != null) {
      val source = Source.fromURL(url)

      properties = new Properties()
      properties.load(source.bufferedReader())

      val scalaProps = properties.asScala


      scalaProps.keys.foreach({ x =>
        //for each system, load the appropriate property file & create a connection property object
        val sysUrl = getClass.getResource(s"/$x.properties")
        if (sysUrl != null) {
          val sysSource = Source.fromURL(sysUrl)

          val connectionProperties = new Properties()
          connectionProperties.load(sysSource.bufferedReader())
          //any extra properties
          connectionProperties.setProperty("fetchsize", "250000")
          //connectionProperties.setProperty("numPartitions", "8")

          //now load the tables
          var tableLoaded = ListBuffer[String]()

          scalaProps.getOrElse(x, "empty").asInstanceOf[String].split(",").foreach { table => {

            if (!tableLoaded.contains(table)) {

              val df = spark.read.jdbc(connectionProperties.get("url").asInstanceOf[String], s"(select * from $table) as $table", connectionProperties)

              df.createTempView(table)

              tableLoaded.append(table)
            }
          }
          }

        }
      })


      //here we have now all our tables loaded, hence we can load our query (e.g., q3.sql)
      // of course you can expect query, table distribution (td) and pg properties, outputpath as input params (args)

      val sqlQuery = scala.io.Source.fromFile(s"src/main/resources/q3.sql").getLines().mkString

      val d = spark.sql(sqlQuery)

      val outputPath = "/tmp/test.csv"

      d.write
        .mode(SaveMode.Overwrite)
        .format("com.databricks.spark.csv")
        .option("header", "true")
        .save(outputPath)
      spark.stop()

    }

  }
}

