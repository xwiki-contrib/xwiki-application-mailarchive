Mail Archive application repository.
====================================

* xwiki-contrib-mail: API (jar) based on Javamail used to parse and extract content from an email.
* xwiki-contrib-mailarchive-api: The core Mail Archive component (jar), loads emails from servers and stores them as wiki pages.
* xwiki-contrib-mailarchive-ui: The UI extension (xar) that provides pages to configure/administrate/navigate the Mail Archive.
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

1. Push all jars (except from tests) to WEB-INF/lib of your xwiki container

2. Import the .xar

3. Browse to following pages:

* MailArchive.Admin: to register needed configuration (servers, lists, types) - some elements are also automatically created when visiting the page.

* MailArchive.Loading: to test servers connections and try loading some mails.

* MailArchive.WebHome: to navigate into loaded topics and emails.
