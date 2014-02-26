Mail Archive application repository.
====================================

* xwiki-contrib-mail: API (jar) based on Javamail used to parse and extract content from an email.
* xwiki-contrib-mailarchive-api: The core Mail Archive component (jar), loads emails from servers and stores them as wiki pages.
* xwiki-contrib-mailarchive-ui: The UI extension (xar) that provides pages to navigate the Mail Archive.
* xwiki-contrib-mailarchive-admin-ui: The UI extension (xar) that provides administration pages for the Mail Archive.
* mstor-custom: needed to modify transitivity of mstor library - used to parse and write to mbox format files.
* xwiki-contrib-mailarchive-test: Automated integration tests based on Selenium and XWiki framework.

Build
-----

1. Configure maven repositories for XWiki: http://dev.xwiki.org/xwiki/bin/view/Community/Building

2. run "mvn clean" at root folder

3. run "mvn install" at root folder

4. to run also "integration tests", run "mvn install -P integration-tests"

5. to deliver a release, run "mvn deploy"

For testing
-----------

1. The easiest, is to configure your local ~/.m2 repository as an extension repository in xwiki.properties,
   then to install the application with Extension Manager application by searching for extension with
   id org.xwiki.contrib.mailarchive:xwiki-contrib-mailarchive-admin-ui (and appropriate version).

2. Browse to following pages:

* XWiki Global Administration / Mail Archive: to register needed configuration (servers, lists, types) - some elements are also automatically created when visiting the page (for now, so you'd better go there first).

* MailArchive.Loading: to test servers connections and try loading some mails. Note: new UI development is in progress for loading, can be seen in page MailArchiveTests.Loading

* MailArchive.WebHome: to navigate into loaded topics and emails.
