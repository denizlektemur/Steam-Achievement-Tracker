
# Steam-Achiement-Tracker

A project used to track missing or already obtained achievements in a given steam library with the ability to sort games into groups.




## Current API endpoints

### Users

| Type      | Command                      | Description                              |
| :-------- | :--------------------------- | :--------------------------------------- |
| `GET`     | `/api/users`                 | Returns all users                        |
| `GET`     | `/api/users/{id}`            | Returns the user with the given id       |
| `GET`     | `/api/users/steam/{steamId}` | Returns the user with the given Steam id |
| `POST`    | `/api/users'                 | Creates a new user                       |
| `DELETE`  | `/api/users/{id}`            | Deletes the user with the given id       |

### Games

| Type | Command     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `GET`     | `/api/games` | Returns all games |
| `GET`     | `/api/games/{id}` | Returns the game with the given id |
| `GET`     | `/api/games/app/{appId}` | Returns the game with the given Steam app id |
| `POST`    | `/api/games` | Creates a new game |
| `DELETE`  | `/api/games/{id}` | Deletes the game with the given id |

### User Library

| Type | Command | Description |
| --- | --- | --- |
| ``GET`` | ``/api/users/{userId}/games`` | Returns all games in the user’s library |
| ``GET`` | ``/api/users/{userId}/games/status/{status}`` | Returns all games filtered by play status |
| ``POST`` | ``/api/users/{userId}/games/{gameId}`` | Adds a game to the user’s library |
| ``PATCH`` | ``/api/users/{userId}/games/{gameId}/status`` | Updates the user’s play status for a specific game |
| ``DELETE`` | ``/api/users/{userId}/games/{gameId}`` | Removes a game from the user’s library |

### Achievements
| Type | Command | Description |
| --- | --- | --- |
| ``GET`` | ``/api/games/{gameId}/achievements`` | Returns all achievements for a game |
| ``GET`` | ``/api/games/{gameId}/achievements/{achievementId}`` | Returns a specific achievement for a game |
| ``GET`` | ``/api/games/{gameId}/achievements/count`` | Returns the number of achievements for the game |

### User Achievements

| Type | Command | Description |
| --- | --- | --- |
| ``GET`` | ``/api/users/{userId}/achievements`` | Returns all achievements unlocked by the user |
| ``GET`` | ``/api/users/{userId}/games/{gameId}/achievements`` | Returns all achievements for a specific game for the user |
| ``GET`` | ``/api/users/{userId}/games/{gameId}/achievements/progress`` | Returns progress stats for the user in that game |
| ``POST`` | ``/api/users/{userId}/achievements/{achievementId}/unlock`` | Marks an achievement as unlocked for the user |
| ``DELETE`` | ``/api/users/{userId}/achievements/{achievementId}`` | Removes an achievement for the user |

