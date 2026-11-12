# Chapter House Online Bookstore

## Technology stack

- Java 17 language target; JDK 23 can be used to run the project.
- Spring Boot 3.5.6 with Spring Web and embedded Tomcat.
- Thymeleaf server-rendered HTML templates.
- HTML, CSS, and JavaScript for the storefront UI.
- Maven for dependency management and packaging.
- UTF-8 text files for persistence. No database is used.

The existing servlet routes, filters, services, repositories, and on-disk data format are retained and registered by Spring Boot. JSP views have been replaced with Thymeleaf templates. This keeps the existing CRUD behavior and file data while allowing the project to start as a Spring Boot application.

## File storage

The existing data directory and text file formats are unchanged. The application resolves its data directory in this order:

1. `-Dapp.data.dir`
2. `APP_DATA_DIR`
3. `%USERPROFILE%\chapter-house-data`

The files are `users.txt`, `books.txt`, `categories.txt`, `reviews.txt`, `wishlist.txt`, and `contact-messages.txt`. 

## Administrator setup

To create an administrator, configure the JVM options
`-Dchapterhouse.bootstrapAdminEmail=<email>` and `-Dchapterhouse.bootstrapAdminPassword=<password>` before starting it.
If the data directory already has users but no administrator, these options create the configured administrator without
replacing existing accounts. If the configured email is already registered, startup reports an error instead of changing
that account's role.

## CRUD features

| Area | Create | Read | Update | Delete | Data file |
|------|--------|------|--------|--------|-----------|
| Accounts | Register | My account | Profile/password | Delete account | `users.txt` |
| Books | Admin add | Shop/detail/admin | Admin edit | Admin delete | `books.txt` |
| Categories | Admin add | Public/admin | Admin edit | Admin delete | `categories.txt` |
| Reviews | Member write | Public/my | Author edit | Author/admin | `reviews.txt` |

Wishlist, contact messages, and login/logout are supporting features.

## Run in IntelliJ IDEA

1. Open the project folder and load it as a Maven project.
2. Set the Project SDK to JDK 23 (the Maven compiler target remains Java 17).
3. Run `lk.chapterhouse.ChapterHouseApplication`, or use the Maven goal `spring-boot:run`.
4. Open `http://localhost:8081/`.

To build a runnable JAR, run `mvn package`, then start `target\online-bookstore.jar`. To choose a data directory, set the JVM option `-Dapp.data.dir=<path>` before starting the application.
