package aggregatorParser

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, HCursor, Json, ParsingFailure}
import zio.{Task, ZIO}

import java.io.{InputStream, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}
import java.util.Scanner
import zio.json
import zio.json.{DecoderOps, JsonDecoder}
import io.circe.parser._
import zio.json.ast.JsonCursor.DownField

object CianParser extends AggregatorParser {

    def getPageApartmentsJson(page: Int) = {

        val url = new URL("https://api.cian.ru/search-offers/v2/search-offers-desktop/")
        val httpConn = url.openConnection.asInstanceOf[HttpURLConnection]
        httpConn.setRequestMethod("POST")

        httpConn.setRequestProperty("authority", "api.cian.ru")
        httpConn.setRequestProperty("accept", "*/*")
        httpConn.setRequestProperty("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
        httpConn.setRequestProperty("content-type", "text/plain;charset=UTF-8")
        httpConn.setRequestProperty("cookie", "_CIAN_GK=dfcfc2df-dca4-4849-98ee-6a46748d8954; session_region_id=1; login_mro_popup=1; _ga=GA1.2.391488065.1667158809; _gid=GA1.2.894530781.1667158809; sopr_utm=%7B%22utm_source%22%3A+%22google%22%2C+%22utm_medium%22%3A+%22organic%22%7D; uxfb_usertype=searcher; tmr_lvid=70ba0f661b0e1d0c7b58c6d6c9db745e; tmr_lvidTS=1630411085447; _ym_uid=1630411427243206670; _ym_d=1667158810; afUserId=c68d9454-f4f8-49cc-96e4-336a014652e5-p; AF_SYNC=1667158810216; adrcid=Ayv54DNVfzD0TEtcfXm9b4Q; _cc_id=baf21f8c6b022a8e371272e2d7847915; panoramaId_expiry=1667763611070; panoramaId=94ceb95a3e215f8c56c72db5ec1016d539383db30c3a64ce74fa95487a66a0c4; session_main_town_region_id=1; _gcl_au=1.1.1879688524.1667158867; cookie_agreement_accepted=1; distance_calculating_onboarding_counter=1; _tt_enable_cookie=1; _ttp=178b139f-07f1-405f-9e17-91d1a13d1a13; _gpVisits={\"isFirstVisitDomain\":true,\"todayD\":\"Mon%20Oct%2031%202022\",\"idContainer\":\"10002511\"}; __cf_bm=qbYePW104OIlvFycRQhzo8eiK372GI5e_Ho4AUaueEE-1667245383-0-Af35O9iVrYky1gBeVQI3XTmFGbo7Clvg5ulRWY1/gUiTY2ScoKid3xHhVcy2fdRRwRv7hBeKmvsHkHtRS2EQM2c=; sopr_session=0d979a735362499f; _gp10002511={\"hits\":5,\"vc\":1,\"ac\":1,\"a6\":1}; _ym_visorc=b; _ym_isad=2; tmr_reqNum=352")
        httpConn.setRequestProperty("origin", "https://www.cian.ru")
        httpConn.setRequestProperty("referer", "https://www.cian.ru/")
        httpConn.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"")
        httpConn.setRequestProperty("sec-ch-ua-mobile", "?0")
        httpConn.setRequestProperty("sec-ch-ua-platform", "\"Windows\"")
        httpConn.setRequestProperty("sec-fetch-dest", "empty")
        httpConn.setRequestProperty("sec-fetch-mode", "cors")
        httpConn.setRequestProperty("sec-fetch-site", "same-site")
        httpConn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36")

        httpConn.setDoOutput(true)
        val writer = new OutputStreamWriter(httpConn.getOutputStream)
        writer.write(s"{\"jsonQuery\":{\"_type\":\"flatsale\",\"engine_version\":{\"type\":\"term\",\"value\":2},\"room\":{\"type\":\"terms\",\"value\":[1,2,3,4]},\"for_day\":{\"type\":\"term\",\"value\":\"!1\"},\"page\":{\"type\":\"term\",\"value\":$page}}}")
        writer.flush()
        writer.close()
        httpConn.getOutputStream.close()

        val responseStream = if (httpConn.getResponseCode / 100 == 2) httpConn.getInputStream
        else httpConn.getErrorStream
        val s = new Scanner(responseStream).useDelimiter("\\A")
        val response = if (s.hasNext) s.next
        else ""
        zio.Console.printLine(response)
    }

    def getAllApartmentsJson: Task[String] = {
        val url = new URL("https://api.cian.ru/search-offers/v2/search-offers-desktop/")
        val httpConn = url.openConnection.asInstanceOf[HttpURLConnection]
        httpConn.setRequestMethod("POST")
        httpConn.setRequestProperty("authority", "api.cian.ru")
        httpConn.setRequestProperty("accept", "*/*")
        httpConn.setRequestProperty("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
        httpConn.setRequestProperty("content-type", "text/plain;charset=UTF-8")
        httpConn.setRequestProperty(
          "cookie",
          "_CIAN_GK=dfcfc2df-dca4-4849-98ee-6a46748d8954; session_region_id=1; login_mro_popup=1; _ga=GA1.2.391488065.1667158809; _gid=GA1.2.894530781.1667158809; sopr_utm=%7B%22utm_source%22%3A+%22google%22%2C+%22utm_medium%22%3A+%22organic%22%7D; sopr_session=0e6d6f419c014a26; uxfb_usertype=searcher; tmr_lvid=70ba0f661b0e1d0c7b58c6d6c9db745e; tmr_lvidTS=1630411085447; _ym_uid=1630411427243206670; _ym_d=1667158810; _ym_isad=2; _ym_visorc=b; _gpVisits={\"isFirstVisitDomain\":true,\"todayD\":\"Sun%20Oct%2030%202022\",\"idContainer\":\"10002511\"}; afUserId=c68d9454-f4f8-49cc-96e4-336a014652e5-p; AF_SYNC=1667158810216; adrdel=1; adrcid=Ayv54DNVfzD0TEtcfXm9b4Q; _cc_id=baf21f8c6b022a8e371272e2d7847915; panoramaId_expiry=1667763611070; panoramaId=94ceb95a3e215f8c56c72db5ec1016d539383db30c3a64ce74fa95487a66a0c4; session_main_town_region_id=1; _gcl_au=1.1.1879688524.1667158867; _gp10002511={\"hits\":2,\"vc\":1,\"ac\":1,\"a6\":1}; cookie_agreement_accepted=1; __cf_bm=odcTuTAkNQIeYz3C_LU6X.ZND7ydJhrguBkVgt5zWWs-1667159901-0-AVCQ+0oNnP0BCoZw695URR5PPrGX2ScTJt91iHsLeYWEsCe9tMyee/UDew74yklvvOfT+NZxzB7Log5sb3RgsfA=; tmr_reqNum=315; _dc_gtm_UA-30374201-1=1"
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
          "{\"jsonQuery\":{\"_type\":\"flatsale\",\"engine_version\":{\"type\":\"term\",\"value\":2},\"region\":{\"type\":\"terms\",\"value\":[1]},\"room\":{\"type\":\"terms\",\"value\":[1,2,3,4,5,6,7,9]},\"for_day\":{\"type\":\"term\",\"value\":\"!1\"}}}")
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

        json.JsonDecoder


//        println(json.ast.Json.decoder.decodeJson(response))


        val doc: Json = parse(response).getOrElse(Json.Null)

        val cursor: HCursor = doc.hcursor

        case class SubPrice(cianId: String)

        object SubPrice {
            implicit val subDecoder: Decoder[SubPrice] = deriveDecoder[SubPrice]
        }


        println(cursor.downField("data").keys)
        println(cursor.downField("data").downField("offersSerialized").downArray.keys)
        println(cursor.downField("data").downField("offersSerialized").downArray.downField("cianId").key)
        println(cursor.downField("data").downField("offersSerialized").downArray.downField("cianId").as[Int])
        println(cursor.downField("data").downField("offersSerialized").as[Seq[SubPrice]].getOrElse(Seq.empty[SubPrice]))
        val d1 = Decoder[Seq[SubPrice]].prepare(_.downField("data").downField("offersSerialized"))
        val d2 = Decoder[SubPrice].prepare(_.downField("data").downField("offersSerialized"))
        val d3 = Decoder[SubPrice]
        cursor.downField("data").downField("offersSerialized").as[Seq[Json]].getOrElse(Seq.empty[Json]) map {
            json => json.hcursor.downField("cianId").as[Int]
                json.hcursor.downField("totalArea").as[String]
            json.hcursor.keys
        } foreach println
        println(decode(response)(d1))
//            .map(_.hcursor.downField("cianId").as[Int]))
        println(cursor.downField("data").downField("offersSerialized").as[Seq[Json]].getOrElse(Seq.empty[Json]).map(_.hcursor.downField("cianId").as[Int]))

        println(cursor.downField("data").downField("offersSerialized").downArray.as[Seq[SubPrice]])

        val smt = Decoder[SubPrice].prepare(_.downField("data").downField("offersSerialized").downArray.downField("cianId"))

        println(decode(response)(smt))


        val decoder = Decoder[String].prepare(_.downField("data").downField("offersSerialized"))
        val str = decode(response)(decoder) match {
            case Left(value) => "Seq.empty[String]"
            case Right(value) => value
        }
        ZIO.attempt(str)
    }

    def getRawJson: Task[String] = {
        val url = new URL("https://api.cian.ru/search-offers/v2/search-offers-desktop/")
        val httpConn: HttpURLConnection = url.openConnection.asInstanceOf[HttpURLConnection]
        httpConn.setRequestMethod("POST")

        httpConn.setRequestProperty("authority", "api.cian.ru")
        httpConn.setRequestProperty("accept", "*/*")
        httpConn.setRequestProperty("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
        httpConn.setRequestProperty("content-type", "text/plain;charset=UTF-8")
        httpConn.setRequestProperty(
          "cookie",
          "_CIAN_GK=dfcfc2df-dca4-4849-98ee-6a46748d8954; session_region_id=1; __cf_bm=jrQO3_QKi0MMK3m8x3g67dQQxX__CdV_6BETrbsL.qc-1667158806-0-AQ7VPXSKE0pvkaMH/lheWb9lFJikn13ZlrBro7+zZ1068DFcdqhPKBLI2KhtQez0c4aidk1qBxWrrGy6I/78JII=; login_mro_popup=1; _ga=GA1.2.391488065.1667158809; _gid=GA1.2.894530781.1667158809; sopr_utm=%7B%22utm_source%22%3A+%22google%22%2C+%22utm_medium%22%3A+%22organic%22%7D; sopr_session=0e6d6f419c014a26; uxfb_usertype=searcher; tmr_lvid=70ba0f661b0e1d0c7b58c6d6c9db745e; tmr_lvidTS=1630411085447; _ym_uid=1630411427243206670; _ym_d=1667158810; _ym_isad=2; _ym_visorc=b; _gpVisits={\"isFirstVisitDomain\":true,\"todayD\":\"Sun%20Oct%2030%202022\",\"idContainer\":\"10002511\"}; afUserId=c68d9454-f4f8-49cc-96e4-336a014652e5-p; AF_SYNC=1667158810216; adrdel=1; adrcid=Ayv54DNVfzD0TEtcfXm9b4Q; _cc_id=baf21f8c6b022a8e371272e2d7847915; panoramaId_expiry=1667763611070; panoramaId=94ceb95a3e215f8c56c72db5ec1016d539383db30c3a64ce74fa95487a66a0c4; session_main_town_region_id=1; _gcl_au=1.1.1879688524.1667158867; _gp10002511={\"hits\":2,\"vc\":1,\"ac\":1,\"a6\":1}; tmr_reqNum=310; cookie_agreement_accepted=1; _dc_gtm_UA-30374201-1=1"
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
          "{\"jsonQuery\":{\"_type\":\"flatsale\",\"engine_version\":{\"type\":\"term\",\"value\":2},\"region\":{\"type\":\"terms\",\"value\":[1]},\"room\":{\"type\":\"terms\",\"value\":[1,2,3,4,5,6,7,9]},\"only_foot\":{\"type\":\"term\",\"value\":\"2\"},\"foot_min\":{\"type\":\"range\",\"value\":{\"lte\":20}},\"balconies\":{\"type\":\"range\",\"value\":{\"gte\":1}},\"is_first_floor\":{\"type\":\"term\",\"value\":false},\"house_material\":{\"type\":\"terms\",\"value\":[1]},\"total_area\":{\"type\":\"range\",\"value\":{\"lte\":50}},\"kitchen\":{\"type\":\"range\",\"value\":{\"lte\":30}},\"repair\":{\"type\":\"terms\",\"value\":[3]}}}")
        writer.flush()
        writer.close()
        httpConn.getOutputStream.close()

        val responseStream: InputStream =
            if (httpConn.getResponseCode / 100 == 2) httpConn.getInputStream
            else httpConn.getErrorStream
        val s: Scanner = new Scanner(responseStream).useDelimiter("\\A")
        val response: String =
            if (s.hasNext) s.next
            else ""

        ZIO.attempt(response)
    }

}
