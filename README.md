Mail Archive application repository.
====================================

* xwiki-contrib-mail: API (jar) based on Javamail used to parse and extract content from an email.
* xwiki-contrib-mailarchive-api: The core Mail Archive component (jar), loads emails from servers and stores them as wiki pages.
* xwiki-contrib-mailarchive-ui: The UI extension (xar) that provides pages to configure/administrate/navigate the Mail Archive.

Build
-----

1. Configure maven repositories for XWiki: http://dev.xwiki.org/xwiki/bin/view/Community/Building

2. run "mvn clean install" at root folder

3. to run also "integration tests", run "mvn clean install -P integration-tests"


For testing
-----------

1. Push xwiki-contrib-mail-*.jar and xwiki-contrib-mailarchive-*.jar to WEB-INF/lib of your xwiki container

2. Import the .xar

3. Browse to following pages:

* MailArchive.Admin: to register needed configuration (servers, lists, types)

* MailArchive.Loading: to test servers connections and try loading some mails

* MailArchive.WebHome: to navigate into loaded topics and emails
