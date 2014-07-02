package com.anisakai.test.pageobjects

import org.openqa.selenium.By
import com.anisakai.test.Config

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 8/26/13
 * Time: 8:44 PM
 * To change this template use File | Settings | File Templates.
 */
object SiteManageTool extends SiteManageTool {
}

class SiteManageTool extends Page {
  def manageAccess(publish: Boolean, globalAccess: Boolean) {
    Portal.xslFrameOne
    click on linkText("Manage Access")
    click on radioButton("publish")

    //TODO actually look at parameters sent in

    click on cssSelector("[value=Update]")
  }


  def createSiteWithSitesTool(siteType:String, siteTitle:String, siteId:String): Boolean = {
    Portal.xslFrameOne
    click on linkText("New Site" )
    textField("id").value = siteId
    textField("title").value = siteTitle
    textField("type").value = siteType
    click on cssSelector("[value=Save]")

       // if we get an error that the site exists, click cancel, that is ok
    if (className("alertMessage").findElement(webDriver).isDefined &&
      className("alertMessage").webElement(webDriver).getText().contains("The site id " + siteId + " is already in use")){
      click on name("eventSubmit_doCancel")
      return false
    }


    return true
  }

  def verifyUserHasRole(eid: String, role: String) {
    // TODO table fun
  }

  def membershipDoesNotExist(eid: String) : Boolean = {
      // if we get an error that the site exists, click cancel, that is ok
    if (className("information").findElement(webDriver).isDefined &&
      className("information").webElement(webDriver).getText().contains("The following participants are already members of this site and cannot be re-added: '" + eid + "'")){
      return false
    }

    return true

  }

  def addUserWithRole(eid: String, role: String){
    if (Config.defaultPortal == "xsl") {
      switch to defaultContent
      switch to frame(1)
    }
    click on linkText("Add Participants" )
    textArea("content::officialAccountParticipant").value = eid
    click on cssSelector("[value=Continue]")

    if (membershipDoesNotExist(eid)) {
      click on cssSelector("[value=" + role + "]")
      click on cssSelector("[value=Continue]")
      click on cssSelector("[value=Continue]")
      click on cssSelector("[value=Finish]")
    } else {
      click on cssSelector("[value=Cancel]")
    }
  }

  def findSiteAndEdit(siteTitle : String): Boolean = {
    var found = false
    Portal.xslFrameOne
    textField("search").value = siteTitle;
    click on cssSelector("[value=Search]")
    if (className("instruction").webElement(webDriver).getText.contains("No sites were found")){
      found = false
    } else {
      checkbox("site1").select()
      click on linkText("Edit")
      found = true
    }
    return found
  }

  def createProjectSite(title: String, shortDescription: String,
                        longDescription: String, contactName: String): String = {
    return createSite("project", shortDescription, longDescription, contactName,
      populateProjectMetaData: (Map[String, String])   => Unit,
      Map(("title", title)))
  }

  def createCourseSite(subject : String, section : String, course : String,
                       shortDescription: String, longDescription: String,
                       contactName: String, contactEmail : String): String = {
    return createSite("course", shortDescription, longDescription, contactName,
      populateCourseMetaData : (Map[String, String]) => Unit,
      Map(("subject", subject), ("section", section), ("course", course)))
  }

  def populateCourseMetaData(args : Map[String, String]) : Unit = {
    textField("id-Subject:1").value = args("subject")
    textField("id-Course:1").value = args("course")
    textField("id-Section:1").value = args("section")
    textField("uniqname").value = "admin"
    click on cssSelector("[value=Continue]")
  }

  def populateProjectMetaData( args : Map[String, String]) {
    textField("title").value = args("title")
  }

  def editSite(shortDescription: String, longDescription: String, contactName: String) {

    click on linkText("Edit Site Information")
    textArea("short_description").value = shortDescription
    textField("siteContactName").value = contactName
    click on cssSelector("[value=Continue]")

    click on cssSelector("[value=Finish]")
 }

  def createSite(siteType: String, shortDescription: String, longDescription: String,
                 contactName: String,
                 siteMetaData: (Map[String, String]) => Unit, siteMetaDataArgs: Map[String, String]): String = {
    Portal.getToFrameZero
    click on linkText("New")
    click on radioButton(siteType.toLowerCase())
    click on id("submitBuildOwn")



    siteMetaData(siteMetaDataArgs)

    textArea("short_description").value = shortDescription
    textField("siteContactName").value = contactName
    click on cssSelector("[value=Continue]")

    click on checkbox("all")
    click on cssSelector("[value=Continue]")
    if (className("emailId").findElement(webDriver).isDefined) {
      textField("emailId").value = faker.lastName() + faker.numerify("####")
    }
    if (className("source_sakai.iframe").findElement(webDriver).isDefined) {
      textField("source_sakai.iframe").value = Config.targetServer
    }
    click on cssSelector("[value=Continue]")
    click on cssSelector("[value=Continue]")

    val siteTitle = xpath("//table[@class='itemSummary']//tr[1]//td[1]").element.text

    click on "addSite"
    eventually {
      Portal.getToFrameZero
    }

    return siteTitle
  }

  def createRandomSite(siteType : String)  : String = {
    if (siteType.equalsIgnoreCase("Course")) {
      return createCourseSite(faker.letterify("???"), faker.numerify("#"), faker.numerify("###"),
        faker.sentence(2), faker.sentence(2), faker.name(), faker.firstName() + "." + faker.lastName())
    } else {
      return createProjectSite(siteType + " Test " + faker.numerify("###"), faker.sentence(2),
        faker.sentence(2), faker.name)
    }

  }


  def addAllTools() {
    Portal.xslFrameOne
    click on linkText("Edit Tools")
    click on checkbox("all")
    click on cssSelector("[value=Continue]")
    if (!webDriver.findElements(By.id("emailId")).isEmpty) {
      textField("emailId").value = faker.lastName() + faker.numerify("####")
    }
    if (!webDriver.findElements(By.id("source_sakai.iframe")).isEmpty) {
      textField("source_sakai.iframe").value = Config.targetServer
    }
    click on cssSelector("[value=Continue]")
    if (Config.defaultPortal == "xsl") {
      click on name("review")
    } else {
      click on cssSelector("[value=Finish]")
    }
    eventually {
      switch to defaultContent
    }

  }

}
