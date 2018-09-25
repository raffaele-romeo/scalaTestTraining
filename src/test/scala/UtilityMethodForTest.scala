import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, render}

trait UtilityMethodForTest {

  def genRandomMessage: Message = {

    val genMessage = {
      import org.scalacheck.Gen.{choose, oneOf}
      for {
        proposition <- oneOf("NOWTV", "SKY", "SKY GO", "SKY Q", "SKY PLUS", null)
        profileId <- choose(1, 10000)
        userType <- oneOf("NON_DTH", null)
        householdId <- choose(1, 10000)
        provider <- oneOf("NOWTV", "SKY", "SKY GO", "SKY Q", "SKY PLUS", null)
        providerTerritory <- oneOf("es", "at", "gb", "ie", null)
        countryCode <- oneOf("es", "at", "gb", "ie", null)
        activityTimestamp <- oneOf("2017-12-11T15:56:47.446Z", "2018-12-11T15:56:47.446Z", null)
      } yield Message(proposition, profileId.toString, userType, householdId.toString,
        provider, providerTerritory, countryCode, activityTimestamp)
    }


    genMessage.sample.get
  }

  def trasformObjectToOriginalJson(message: Message): String = {

    val json =
      ("activityTimestamp" -> message.activityTimestamp) ~
        ("userType" -> message.userType) ~
        ("provider" -> message.provider) ~
        ("providerTerritory" -> message.providerTerritory) ~
        ("proposition" -> message.proposition) ~
        ("userId" -> message.profileId) ~
        ("householdId" -> message.profileId) ~
        ("countryCode" -> message.countryCode)


    message.profileId + "|" + deleteFieldsWithNullValueInJson( compact(render(json)) )

  }

  def trasformObjectToApplicationJson(message: Message): String = {

    val json =
      ("activity_timestamp" -> message.activityTimestamp) ~
        ("profileType" -> message.userType) ~
        ("provider" -> message.provider) ~
        ("providerTerritory" -> message.providerTerritory) ~
        ("proposition" -> message.proposition) ~
        ("profileId" -> message.profileId) ~
        ("householdId" -> message.profileId) ~
        ("countryCode" -> message.countryCode)


    message.profileId + "|" + deleteFieldsWithNullValueInJson( compact(render(json)) )
  }


  def deleteFieldsWithNullValueInJson(jsonMessage: String): String = {

    val jsonString = jsonMessage.split(",").filter(!_.contains("null")).mkString(",")

    if (!jsonString.contains("{") && !jsonString.contains("}")) {
      "{" + jsonString + "}"
    } else if (!jsonString.contains("{")) {
      "{" + jsonString
    } else if (!jsonString.contains("}")) {
      jsonString + "}"
    } else {
      jsonString
    }

  }

}