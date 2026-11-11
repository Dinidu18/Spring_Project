package lk.chapterhouse.service;

import java.util.List;
import java.util.OptionalDouble;

import lk.chapterhouse.model.Review;
import lk.chapterhouse.model.Role;
import lk.chapterhouse.model.User;
import lk.chapterhouse.repository.BookRepository;
import lk.chapterhouse.repository.ReviewRepository;
import lk.chapterhouse.util.AppClock;
import lk.chapterhouse.util.Ids;

public class ReviewService {
    private final ReviewRepository reviews;
    private final BookRepository books;

    public ReviewService(ReviewRepository reviews, BookRepository books) {
        this.reviews = reviews;
        this.books = books;
    }

    public List<Review> listForBook(String bookId) {
        return reviews.findByBook(bookId);
    }

    public List<Review> listForAuthor(String authorId) {
        return reviews.findByAuthor(authorId);
    }

    public Review create(String bookId, String authorId, int rating, String title, String body) {
        books.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found."));
        if (reviews.findByBookAndAuthor(bookId, authorId).isPresent()) {
            throw new IllegalArgumentException("You already reviewed this book.");
        }
        var now = AppClock.now();
        Review review = new Review(Ids.newId(), bookId, authorId, rating, title, body, now, now, 1L);
        return reviews.create(review);
    }

    public Review updateByAuthor(String reviewId, String authorId, int rating, String title, String body) {
        Review review = reviews.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));
        if (!review.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("You can only edit your own reviews.");
        }
        review.setRating(rating);
        review.setTitle(title);
        review.setBody(body);
        ServiceRecords.touchVersioned(review);
        return reviews.update(review);
    }

    public void deleteByAuthor(String reviewId, String authorId) {
        Review review = reviews.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));
        if (!review.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("You can only delete your own reviews.");
        }
        reviews.delete(reviewId);
    }

    public void deleteByAdmin(String reviewId, User admin) {
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Administrator access required.");
        }
        reviews.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Review not found."));
        reviews.delete(reviewId);
    }

    public OptionalDouble averageRatingForBook(String bookId) {
        List<Review> bookReviews = reviews.findByBook(bookId);
        if (bookReviews.isEmpty()) {
            return OptionalDouble.empty();
        }
        double sum = 0;
        for (Review review : bookReviews) {
            sum += review.getRating();
        }
        return OptionalDouble.of(sum / bookReviews.size());
    }
}
