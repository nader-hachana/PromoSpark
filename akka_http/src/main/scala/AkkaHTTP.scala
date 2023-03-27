package com.cognira.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.Row
import scala.collection.JavaConverters._ //asScala
import play.api.libs.json._
import java.net.InetSocketAddress


object AkkaHTTP {
   def main(args: Array[String]): Unit=
   { 
        implicit val system = ActorSystem("AkkaHTTP") // akka actors
        implicit val materializer = ActorMaterializer() // akka streams
        implicit val executionContext = system.dispatcher 

        // Applying cassandra connection
        val session = CqlSession.builder()
                                .addContactPoint(InetSocketAddress.createUnresolved("cassandra", 9042))
                                .withAuthCredentials("cassandra", "cassandra")
                                .withLocalDatacenter("datacenter1")
                                .build()
        // fetching data with get request on path "localhost:8080/api/data/<product purchased>/<promotion category>/<promotion discount>"
        val route = (path("api"/"data"/IntNumber/IntNumber/DoubleNumber) & get) { (prod_purch, promo_cat, promo_discount) =>
          complete {
            val sales_data = session.execute(s"SELECT total_sales_unit_promo_prod, lift_unit, lift_percentage FROM sales.db_sales WHERE prod_purch=$prod_purch and promo_cat = $promo_cat and promo_discount= $promo_discount ALLOW FILTERING;")
                              .all()
                              .asScala.map { row =>
              Json.obj(
                "total_sales_unit_promo_prod" -> row.getLong("total_sales_unit_promo_prod"),
                "lift_unit" -> row.getDouble("lift_unit"),
                "lift_percentage" -> row.getDouble("lift_percentage")
              )
            }
            HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, Json.toJson(sales_data).toString))
          }
        }
    
        Http().bindAndHandle(route, "0.0.0.0", 8080)
   }
}
