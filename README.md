# 🎬 MovieTracker

MovieTracker is a sleek and intuitive Android application designed for movie enthusiasts to discover, track, and manage their favorite films and TV shows. Built with modern Android development practices, it leverages the OMDB API to provide a comprehensive database at your fingertips.

---

## 🚀 Features

-   **Advanced Search**: Explore a vast library of movies and TV shows using the OMDB API with pagination support.
-   **Comprehensive Tracking**: Categorize your content into "Watching", "Completed", "Dropped", and "Planned" lists.
-   **Progress Management**: Keep track of your episode progress for TV shows and rate your completed watches.
-   **Detailed Insights**: View in-depth information including plots, ratings (both IMDb and user-assigned), and cast details.
-   **Personalized Profile**: Visualize your tracking statistics, including type distribution (Movies vs. TV Shows) and average ratings.
-   **Offline Synchronization**: Robust local storage using SQLite ensures your watchlist is always accessible and up-to-date.

---

## 🛠 Tech Stack

-   **Language**: Java
-   **UI Framework**: Android Material Components (BottomNavigationView, TabLayout, CardView, SwipeRefreshLayout)
-   **Networking**: [Retrofit 2](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) with Logging Interceptor
-   **JSON Parsing**: [Gson](https://github.com/google/gson)
-   **Image Loading**: [Glide 4](https://github.com/bumptech/glide)
-   **Database**: SQLite with a multi-version schema (v3) for tracking scores and progress.
-   **Architecture**: Single Activity host with Fragment-based navigation using `BottomNavigationView`.

---

## 📦 Installation & Setup

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/MovieTracker.git
    ```
2.  **Open in Android Studio**:
    Select "Open an Existing Project" and point to the cloned directory.
3.  **API Configuration**:
    -   This app requires an API key from [OMDb API](http://www.omdbapi.com/apikey.aspx).
    -   Insert your API key in the `ApiClient.java` file located at `app/src/main/java/com/example/movietracker/data/api/`.
4.  **Build & Run**:
    Click the **Run** button in Android Studio to deploy the app to your emulator or physical device.

---

## 📊 Database Schema (v3)

The application uses an evolved SQLite schema to support advanced tracking:
-   `imdb_id`: Primary Key
-   `status`: Watch status (WATCHING, COMPLETED, etc.)
-   `episode_progress`: Current episode count for shows.
-   `user_score`: Personal rating for completed content.

---

## 📸 Screenshots

| Search Screen | Watchlist | Profile Stats |
| :---: | :---: | :---: |
| _Placeholder_ | _Placeholder_ | _Placeholder_ |

---

## 🤝 Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request if you have ideas for improvements or find any bugs.

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
