Mail Archive application repository.
====================================

* xwiki-component-mail: API (jar) based on Javamail used to parse and extract content from an email.
* xwiki-component-mailarchive: The core Mail Archive component (jar), loads emails from servers and stores them as wiki pages.
* xwiki-component-mailarchive-ui: The UI extension (xar) that provides pages to configure/administrate/navigate the Mail Archive.

Build
-----

1. run "mvn clean install -Dmaven.test.skip=true" or "mvn clean install -Dmaven.test.failure.ignore=true"

2. Unit tests are almost not implemented, and initialize() of component fails to pass jmock expectations for still unknown reason


For testing
-----------

1. Push xwiki-component-mail-*.jar and xwiki-component-mailarchive-*.jar to WEB-INF/lib of your xwiki container

2. Import the .xar

3. Browse to following pages:

* MailArchive.Admin: to register needed configuration (servers, lists, types)

* MailArchive.Loading: to test servers connections and try loading some mails

* MailArchive.WebHome: to navigate into loaded topics and emails
