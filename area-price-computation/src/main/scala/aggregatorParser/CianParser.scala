package aggregatorParser

import data._
import io.circe.parser._
import io.circe.{HCursor, Json}
import zio.{Task, ZIO}

import java.io.OutputStreamWriter
import java.net.{HttpURLConnection, URL}
import java.util.Scanner
import scala.util.Try

object CianParser extends AggregatorParser {

    def getPageApartmentsJson(page: Int): Task[Json] = {

        val url = new URL("https://api.cian.ru/search-offers/v2/search-offers-desktop/")
        val httpConn = url.openConnection.asInstanceOf[HttpURLConnection]
        httpConn.setRequestMethod("POST")

        httpConn.setRequestProperty("authority", "api.cian.ru")
        httpConn.setRequestProperty("accept", "*/*")
        httpConn.setRequestProperty("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
        httpConn.setRequestProperty("content-type", "text/plain;charset=UTF-8")
        httpConn.setRequestProperty(
          "cookie",
          "_CIAN_GK=dfcfc2df-dca4-4849-98ee-6a46748d8954; session_region_id=1; login_mro_popup=1; _ga=GA1.2.391488065.1667158809; _gid=GA1.2.894530781.1667158809; sopr_utm=%7B%22utm_source%22%3A+%22google%22%2C+%22utm_medium%22%3A+%22organic%22%7D; uxfb_usertype=searcher; tmr_lvid=70ba0f661b0e1d0c7b58c6d6c9db745e; tmr_lvidTS=1630411085447; _ym_uid=1630411427243206670; _ym_d=1667158810; afUserId=c68d9454-f4f8-49cc-96e4-336a014652e5-p; AF_SYNC=1667158810216; adrcid=Ayv54DNVfzD0TEtcfXm9b4Q; _cc_id=baf21f8c6b022a8e371272e2d7847915; panoramaId_expiry=1667763611070; panoramaId=94ceb95a3e215f8c56c72db5ec1016d539383db30c3a64ce74fa95487a66a0c4; session_main_town_region_id=1; _gcl_au=1.1.1879688524.1667158867; cookie_agreement_accepted=1; distance_calculating_onboarding_counter=1; _tt_enable_cookie=1; _ttp=178b139f-07f1-405f-9e17-91d1a13d1a13; _gpVisits={\"isFirstVisitDomain\":true,\"todayD\":\"Mon%20Oct%2031%202022\",\"idContainer\":\"10002511\"}; __cf_bm=qbYePW104OIlvFycRQhzo8eiK372GI5e_Ho4AUaueEE-1667245383-0-Af35O9iVrYky1gBeVQI3XTmFGbo7Clvg5ulRWY1/gUiTY2ScoKid3xHhVcy2fdRRwRv7hBeKmvsHkHtRS2EQM2c=; sopr_session=0d979a735362499f; _gp10002511={\"hits\":5,\"vc\":1,\"ac\":1,\"a6\":1}; _ym_visorc=b; _ym_isad=2; tmr_reqNum=352"
        )
        httpConn.setRequestProperty("origin", "https://www.cian.ru")
        httpConn.setRequestProperty("referer", "https://www.cian.ru/")
        httpConn.setRequestProperty(
          "sec-ch-ua",
          "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"")
        httpConn.setRequestProperty("sec-ch-ua-mobile", "?0")
        httpConn.setRequestProperty("sec-ch-ua-platform", "\"Windows\"")
        httpConn.setRequestProperty("sec-fetch-dest", "empty")
        httpConn.setRequestProperty("sec-fetch-mode", "cors")
        httpConn.setRequestProperty("sec-fetch-site", "same-site")
        httpConn.setRequestProperty(
          "user-agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36")

        httpConn.setDoOutput(true)
        val writer = new OutputStreamWriter(httpConn.getOutputStream)
        writer.write(
          s"{\"jsonQuery\":{\"_type\":\"flatsale\",\"engine_version\":{\"type\":\"term\",\"value\":2},\"room\":{\"type\":\"terms\",\"value\":[1,2,3,4]},\"for_day\":{\"type\":\"term\",\"value\":\"!1\"},\"page\":{\"type\":\"term\",\"value\":$page}}}")
        writer.flush()
        writer.close()
        httpConn.getOutputStream.close()

        val responseStream =
            if (httpConn.getResponseCode / 100 == 2) httpConn.getInputStream
            else httpConn.getErrorStream
        val s = new Scanner(responseStream).useDelimiter("\\A")
        val response =
            if (s.hasNext) s.next
            else ""
        ZIO.attempt(parse(response).getOrElse(Json.Null))
    }

    def parseJson(json: Json): Task[Seq[Apartment]] = {

        val cursor: HCursor = json.hcursor

        val seqOffers = cursor.downField("data").downField("offersSerialized").as[Seq[Json]].getOrElse(Seq.empty[Json])
        val seqApartments = seqOffers map { json =>
            val under = json.hcursor.downField("geo").downField("undergrounds").as[Seq[Json]].getOrElse(Seq.empty[Json])
            val address = json.hcursor.downField("geo").downField("address").as[Seq[Json]].getOrElse(Seq.empty[Json])

            def partialApplyFunc[A](f: Json => Option[A]): Option[A] = tryParseField(json)(f).flatten

            Apartment(
              partialApplyFunc(_.hcursor.downField("cianId").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("fullUrl").as[String].toOption),
              (for {
                  lng <- json.hcursor.downField("geo").downField("coordinates").downField("lng").as[Double]
                  lat <- json.hcursor.downField("geo").downField("coordinates").downField("lat").as[Double]
              } yield Coordinates(lng, lat)).toOption,
              partialApplyFunc(_.hcursor.downField("bargainTerms").downField("priceRur").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("roomsCount").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("category").as[String].toOption),
              partialApplyFunc(_.hcursor.downField("building").downField("materialType").as[String].toOption),
              partialApplyFunc(_.hcursor.downField("floorNumber").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("totalArea").as[Double].toOption),
              partialApplyFunc(_.hcursor.downField("kitchenArea").as[Double].toOption),
              partialApplyFunc(_.hcursor.downField("balconiesCount").as[String].toOption).isDefined,
              under map { jsUnder =>
                  WayToMetro(
                    jsUnder.hcursor.downField("time").as[Int].toOption,
                    jsUnder.hcursor.downField("name").as[String].toOption,
                    jsUnder.hcursor.downField("transportType").as[String].toOption
                  )
              },
              partialApplyFunc(_.hcursor.downField("decoration").as[String].toOption),
              HouseParameters(
                partialApplyFunc(_.hcursor.downField("building").downField("floorsCount").as[Int].toOption),
                address map { add =>
                    Param(
                      add.hcursor.downField("type").as[String].toOption,
                      add.hcursor.downField("title").as[String].toOption
                    )
                }
              )
            )
        }

        ZIO.attempt(seqApartments)

    }

    private def tryParseField[A](json: Json)(f: Json => A): Option[A] = Try(f(json)).toOption

}
