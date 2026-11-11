package lk.chapterhouse.seed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lk.chapterhouse.model.Category;
import lk.chapterhouse.model.EBook;
import lk.chapterhouse.model.PrintedBook;
import lk.chapterhouse.model.User;
import lk.chapterhouse.service.AccountService;
import lk.chapterhouse.service.BookService;
import lk.chapterhouse.service.CategoryService;
import lk.chapterhouse.service.ReviewService;

/**
 * Demo catalog for first-time empty installations.
 * <p>
 * Administrator credentials are never hard-coded: pass
 * {@code -Dchapterhouse.bootstrapAdminEmail} and {@code -Dchapterhouse.bootstrapAdminPassword}
 * when the dataset is empty. Demo member accounts use
 * {@code -Dchapterhouse.seedMemberPassword} (default {@code ChangeMeMember1!} for empty seed only).
 */
public final class SeedData {
    private static final Logger LOGGER = Logger.getLogger(SeedData.class.getName());
    private static final String MEMBER_PASSWORD_PROPERTY = "chapterhouse.seedMemberPassword";
    private static final String DEFAULT_MEMBER_PASSWORD = "ChangeMeMember1!";

    private SeedData() {
    }

    public static void load(AccountService accounts, CategoryService categories,
                            BookService books, ReviewService reviews) {
        User admin = createBootstrapAdmin(accounts);
        char[] memberPassword = resolveMemberPassword();
        User memberOne = accounts.registerMember("Ada Reader", "ada.reader@example.com", "0771234567", memberPassword);
        User memberTwo = accounts.registerMember("Ben Bibliophile", "ben.bibliophile@example.com", "0777654321", memberPassword);
        User memberThree = accounts.registerMember("Cora Page", "cora.page@example.com", "0775551212", memberPassword);

        List<Category> seededCategories = seedCategories(categories);
        List<String> bookIds = seedBooks(books, seededCategories);
        seedSampleReviews(reviews, memberOne, memberTwo, memberThree, bookIds);

        LOGGER.log(Level.INFO, "Seed dataset loaded ({0} categories, {1} books). Admin present: {2}",
                new Object[]{seededCategories.size(), bookIds.size(), admin != null});
    }

    private static User createBootstrapAdmin(AccountService accounts) {
        String email = System.getProperty("chapterhouse.bootstrapAdminEmail");
        String password = System.getProperty("chapterhouse.bootstrapAdminPassword");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            LOGGER.log(Level.WARNING,
                    "Skipping seed admin: set chapterhouse.bootstrapAdminEmail and chapterhouse.bootstrapAdminPassword.");
            return null;
        }
        return accounts.createAdmin("Chapter House Admin", email, "0112345678", password.toCharArray());
    }

    private static char[] resolveMemberPassword() {
        String configured = System.getProperty(MEMBER_PASSWORD_PROPERTY);
        if (configured == null || configured.isBlank()) {
            return DEFAULT_MEMBER_PASSWORD.toCharArray();
        }
        return configured.toCharArray();
    }

    private static List<Category> seedCategories(CategoryService categories) {
        List<Category> created = new ArrayList<>();
        created.add(categories.create("Fiction", "Novels and short stories spanning literary and popular fiction.", "categories/fiction.jpg"));
        created.add(categories.create("Science & Technology", "Computing, engineering, and science titles for curious readers.", "categories/science-tech.jpg"));
        created.add(categories.create("Business", "Leadership, entrepreneurship, and professional development books.", "categories/business.jpg"));
        created.add(categories.create("Children", "Picture books and middle-grade adventures for young readers.", "categories/children.jpg"));
        created.add(categories.create("History", "World history, biography, and cultural studies across eras.", "categories/history.jpg"));
        created.add(categories.create("Self Help", "Mindfulness, productivity, and personal growth guides.", "categories/self-help.jpg"));
        created.add(categories.create("Mystery", "Coastal thrillers, quiet whodunits, and late-night page-turners.", "categories/mystery.jpg"));
        created.add(categories.create("Travel", "Journeys, field notes, and place-written memoirs.", "categories/travel.jpg"));
        created.add(categories.create("Poetry", "Collections for reading aloud, slowly, and often.", "categories/poetry.jpg"));
        return created;
    }

    private static List<String> seedBooks(BookService books, List<Category> categories) {
        String fiction = categories.get(0).getId();
        String tech = categories.get(1).getId();
        String business = categories.get(2).getId();
        String children = categories.get(3).getId();
        String history = categories.get(4).getId();
        String selfHelp = categories.get(5).getId();
        String mystery = categories.get(6).getId();
        String travel = categories.get(7).getId();
        String poetry = categories.get(8).getId();

        List<String> ids = new ArrayList<>();

        ids.add(books.createPrinted("The Lantern Archive", "Maya Perera",
                "A Colombo archivist uncovers letters that rewrite a family saga across three generations of quiet courage.",
                "9780134685991", 2019, fiction, bd("24.99"), "covers/lantern-archive.jpg", true,
                12, 368, PrintedBook.PAPERBACK).getId());

        ids.add(books.createPrinted("Monsoon Letters", "Ruwan Silva",
                "Epistolary fiction set during a turbulent monsoon season along the south coast, full of salt and longing.",
                "9780132350884", 2016, fiction, bd("18.50"), "covers/monsoon-letters.jpg", false,
                4, 292, PrintedBook.PAPERBACK).getId());

        ids.add(books.createEbook("Cloud Patterns", "Anika Fernando",
                "Digital-native novel exploring friendship, latency, and late-night video calls across time zones.",
                "9780201633610", 2021, fiction, bd("12.99"), "covers/cloud-patterns.jpg", true,
                EBook.EPUB, bd("4.2"), true).getId());

        ids.add(books.createPrinted("Effective Java", "Joshua Bloch",
                "Best practices for the Java programming language, updated for modern releases and team craft.",
                "9780134686776", 2018, tech, bd("54.00"), "covers/effective-java.jpg", true,
                8, 416, PrintedBook.HARDCOVER).getId());

        ids.add(books.createEbook("Design Patterns Explained", "Alan Shalloway",
                "Introductory guide to object-oriented patterns with clear diagrams and practical examples.",
                "9780321125217", 2004, tech, bd("34.99"), "covers/design-patterns-explained.jpg", false,
                EBook.PDF, bd("22.0"), true).getId());

        ids.add(books.createPrinted("Clean Code", "Robert C. Martin",
                "Readable code techniques for professional software developers and collaborative teams.",
                "9780136083238", 2008, tech, bd("47.50"), "covers/clean-code.jpg", true,
                15, 464, PrintedBook.PAPERBACK).getId());

        ids.add(books.createEbook("Site Reliability Engineering", "Niall Murphy",
                "Google SRE practices for building reliable large-scale systems and thoughtful on-call culture.",
                "9781492037255", 2016, tech, bd("29.99"), "covers/sre.jpg", true,
                EBook.EPUB, bd("15.8"), true).getId());

        ids.add(books.createPrinted("The Lean Startup", "Eric Ries",
                "Entrepreneurship framework for validated learning and iterative product delivery.",
                "9781617294945", 2011, business, bd("19.99"), "covers/lean-startup.jpg", true,
                20, 336, PrintedBook.PAPERBACK).getId());

        ids.add(books.createPrinted("Deep Work", "Cal Newport",
                "Strategies for focused work in a distracted world for knowledge workers and makers.",
                "9781400000043", 2016, business, bd("16.50"), "covers/deep-work.jpg", false,
                7, 304, PrintedBook.HARDCOVER).getId());

        ids.add(books.createEbook("Atomic Habits", "James Clear",
                "Small daily habits compound into remarkable long-term personal results without burnout theater.",
                "9780735211292", 2018, selfHelp, bd("14.99"), "covers/atomic-habits.jpg", true,
                EBook.EPUB, bd("6.1"), true).getId());

        ids.add(books.createPrinted("The Kite Runner", "Khaled Hosseini",
                "Friendship and redemption set against Afghanistan's changing political landscape.",
                "9781594480003", 2003, fiction, bd("15.25"), "covers/kite-runner.jpg", false,
                6, 371, PrintedBook.PAPERBACK).getId());

        ids.add(books.createEbook("Sapiens", "Yuval Noah Harari",
                "A brief history of humankind from cognitive revolution to modern networks and shared myths.",
                "9780062316097", 2015, history, bd("18.75"), "covers/sapiens.jpg", true,
                EBook.PDF, bd("12.4"), true).getId());

        ids.add(books.createPrinted("The Book Thief", "Markus Zusak",
                "A girl in Nazi Germany steals books and shares them with neighbors in secret, narrated by Death.",
                "9780375831003", 2006, children, bd("11.99"), "covers/book-thief.jpg", true,
                25, 552, PrintedBook.PAPERBACK).getId());

        ids.add(books.createPrinted("Goodnight Moon Room", "Lila Brooks",
                "Gentle bedtime story about a child saying goodnight to every corner of a soft and familiar home.",
                "9781400000005", 2020, children, bd("9.50"), "covers/goodnight-room.jpg", true,
                30, 32, PrintedBook.HARDCOVER).getId());

        ids.add(books.createEbook("Mindful Mornings", "Priya Nadesan",
                "Short guided reflections to start each day with calm intention, breath, and focus.",
                "9781400000012", 2022, selfHelp, bd("8.99"), "covers/mindful-mornings.jpg", false,
                EBook.EPUB, bd("2.5"), true).getId());

        ids.add(books.createPrinted("Colonial Routes", "David Mendis",
                "Trade, migration, and memory along Indian Ocean ports in the nineteenth century.",
                "9781400000029", 2014, history, bd("27.00"), "covers/colonial-routes.jpg", false,
                4, 512, PrintedBook.HARDCOVER).getId());

        ids.add(books.createEbook("Startup Finance Basics", "Helen Cho",
                "Cash flow, unit economics, and fundraising essentials for first-time founders.",
                "9781400000036", 2023, business, bd("21.50"), "covers/startup-finance.jpg", true,
                EBook.PDF, bd("8.0"), true).getId());

        ids.add(books.createPrinted("The Pragmatic Programmer", "Andrew Hunt",
                "Tips and philosophies for becoming a more effective software craftsman and teammate.",
                "9780135957059", 2019, tech, bd("49.99"), "covers/pragmatic-programmer.jpg", true,
                9, 352, PrintedBook.PAPERBACK).getId());

        ids.add(books.createPrinted("Harbor of Secrets", "Nisha Jayasuriya",
                "A rainy-port mystery where a missing ferry passenger leaves behind a notebook of unfinished names.",
                "9780143127550", 2024, mystery, bd("17.99"), "covers/harbor-secrets.jpg", true,
                14, 318, PrintedBook.PAPERBACK).getId());

        ids.add(books.createEbook("Night Market Notes", "Theo Rahman",
                "Street-food stalls, rumor networks, and a detective who listens better than she speaks.",
                "9780307474278", 2023, mystery, bd("11.50"), "covers/night-market.jpg", true,
                EBook.EPUB, bd("3.8"), true).getId());

        ids.add(books.createPrinted("Tea Trails", "Camille Ortiz",
                "Walking the misted estates of hill country with notebooks, thermos flasks, and patient curiosity.",
                "9780061120084", 2022, travel, bd("22.00"), "covers/tea-trails.jpg", true,
                11, 276, PrintedBook.HARDCOVER).getId());

        ids.add(books.createEbook("Garden of Maps", "Jonah West",
                "Field essays on getting lost on purpose — cities, footpaths, and the kindness of strangers.",
                "9781451673319", 2021, travel, bd("13.25"), "covers/garden-of-maps.jpg", false,
                EBook.PDF, bd("7.4"), true).getId());

        ids.add(books.createPrinted("Salt and Cinnamon", "Ishan De Silva",
                "Poems of kitchen tables, coastal wind, and the small ceremonies that keep a family stitched.",
                "9780143038412", 2020, poetry, bd("14.00"), "covers/salt-cinnamon.jpg", true,
                18, 96, PrintedBook.PAPERBACK).getId());

        ids.add(books.createEbook("Quiet Algorithms", "Mira Cole",
                "Lyric poems about machines that almost listen, and people who almost reply.",
                "9780385537858", 2024, poetry, bd("9.75"), "covers/quiet-algorithms.jpg", true,
                EBook.EPUB, bd("1.9"), true).getId());

        return ids;
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static void seedSampleReviews(ReviewService reviews, User memberOne, User memberTwo,
                                          User memberThree, List<String> bookIds) {
        if (bookIds.size() < 8) {
            return;
        }
        reviews.create(bookIds.get(0), memberOne.getId(), 5, "Beautifully written",
                "I could not put this down; the characters feel real and the pacing is perfect for rainy evenings.");
        reviews.create(bookIds.get(1), memberTwo.getId(), 4, "Atmospheric read",
                "The monsoon setting is vivid, though the middle section slows a little before a strong ending.");
        reviews.create(bookIds.get(2), memberOne.getId(), 4, "Modern and warm",
                "A thoughtful take on digital friendships without feeling preachy or overly clever.");
        reviews.create(bookIds.get(3), memberThree.getId(), 5, "Desk-side essential",
                "Clear advice I still reopen when a design review gets messy. Worth every bookmark.");
        reviews.create(bookIds.get(9), memberTwo.getId(), 5, "Tiny changes, big calm",
                "Practical without being loud. The habit stacking chapters changed my mornings.");
        reviews.create(bookIds.get(18), memberThree.getId(), 5, "Could not sleep",
                "Harbor fog, ferry horns, and a mystery that trusts the reader. Finished in one night.");
        reviews.create(bookIds.get(20), memberOne.getId(), 4, "Travel that breathes",
                "Tea Trails smells like wet leaves and thermos coffee. Lovely pace for weekend reading.");
        reviews.create(bookIds.get(22), memberTwo.getId(), 5, "Poems to reread",
                "Salt and Cinnamon has lines I copied into my notebook. Quiet and generous.");
    }
}
