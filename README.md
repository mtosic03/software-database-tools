This project implements a relational movie management system designed for advanced data modeling, integrity enforcement, and user interaction analysis.
The database models users, movies, genres, and tags, supporting features such as personalized recommendations, behavioral constraints, and rating analytics. Business rules are enforced using SQL triggers and stored procedures, ensuring data consistency and intelligent behavior within the system.

Key functionalities include:

Enforcing rating constraints and detecting rating bias through triggers.
Generating personalized movie recommendations based on user preferences and genre statistics.
Rewarding users who rate under-appreciated movies within their favorite genres.
Determining user specialization and profile type (“curious”, “focused”, or “undefined”) through aggregate analysis of tag-based ratings.
Maintaining referential integrity and cascading updates across related entities.
The backend is implemented in MS SQL Server, with integration via Java (JDBC) for executing queries, transactions, and procedural logic.
