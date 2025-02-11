package com.michaldrabik.data_local.database.migrations

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

const val DATABASE_VERSION = 42
const val DATABASE_NAME = "SHOWLY2_DB_2"

class Migrations(
  context: Context,
) {

  private val migration2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN show_anticipated_shows INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val migration3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE actors ADD COLUMN id_imdb TEXT")
    }
  }

  private val migration4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_sync_schedule TEXT NOT NULL DEFAULT 'OFF'")
    }
  }

  private val migration5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_running_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_incoming_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_ended_is_enabled INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_shows_recent_is_enabled INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val migration6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("CREATE INDEX index_episodes_id_show_trakt ON episodes(id_show_trakt)")
      database.execSQL("CREATE INDEX index_seasons_id_show_trakt ON seasons(id_show_trakt)")
    }
  }

  private val migration7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_filter_genres TEXT NOT NULL DEFAULT ''")
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_filter_feed TEXT NOT NULL DEFAULT 'HOT'")
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_quick_sync_enabled INTEGER NOT NULL DEFAULT 0")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `trakt_sync_queue` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, " +
          "`type` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, " +
          "`updated_at` INTEGER NOT NULL)",
      )
    }
  }

  private val migration8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN watchlist_sort_by TEXT NOT NULL DEFAULT 'NAME'")
    }
  }

  private val migration9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN trakt_quick_remove_enabled INTEGER NOT NULL DEFAULT 0")
    }
  }

  private val migration10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `shows_archive` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, " +
          "`created_at` INTEGER NOT NULL, " +
          "`updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE UNIQUE INDEX index_shows_archive_id_trakt ON shows_archive(id_trakt)")
      database.execSQL("ALTER TABLE settings ADD COLUMN archive_shows_sort_by TEXT NOT NULL DEFAULT 'NAME'")
    }
  }

  private val migration11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN archive_shows_include_statistics INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val migration12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN special_seasons_enabled INTEGER NOT NULL DEFAULT 0")
    }
  }

  private val migration13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `shows_translations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `title` TEXT NOT NULL, `language` TEXT NOT NULL, `overview` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE UNIQUE INDEX index_shows_translations_id_trakt ON shows_translations(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `episodes_translations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `id_trakt_show` INTEGER NOT NULL, " +
          "`title` TEXT NOT NULL, `language` TEXT NOT NULL, `overview` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt_show`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE UNIQUE INDEX index_episodes_translations_id_trakt ON episodes_translations(id_trakt)")
      database.execSQL("CREATE INDEX index_episodes_translations_id_trakt_show ON episodes_translations(id_trakt_show)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_translations_log` (" +
          "`id_show_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`synced_at` INTEGER NOT NULL)",
      )
    }
  }

  private val migration14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE shows_images ADD COLUMN source TEXT NOT NULL DEFAULT 'tvdb'")
    }
  }

  private val migration15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN show_anticipated_movies INTEGER NOT NULL DEFAULT 0")
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_movies_filter_genres TEXT NOT NULL DEFAULT ''")
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_movies_filter_feed TEXT NOT NULL DEFAULT 'HOT'")
      database.execSQL("ALTER TABLE settings ADD COLUMN my_movies_all_sort_by TEXT NOT NULL DEFAULT 'NAME'")
      database.execSQL("ALTER TABLE settings ADD COLUMN see_later_movies_sort_by TEXT NOT NULL DEFAULT 'NAME'")
      database.execSQL("ALTER TABLE settings ADD COLUMN progress_movies_sort_by TEXT NOT NULL DEFAULT 'NAME'")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies` (" +
          "`id_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`id_tmdb` INTEGER NOT NULL DEFAULT -1, " +
          "`id_imdb` TEXT NOT NULL DEFAULT '', " +
          "`id_slug` TEXT NOT NULL DEFAULT '', " +
          "`title` TEXT NOT NULL DEFAULT '', " +
          "`year` INTEGER NOT NULL DEFAULT -1, " +
          "`overview` TEXT NOT NULL DEFAULT '', " +
          "`released` TEXT NOT NULL DEFAULT '', " +
          "`runtime` INTEGER NOT NULL DEFAULT -1, " +
          "`country` TEXT NOT NULL DEFAULT '', " +
          "`trailer` TEXT NOT NULL DEFAULT '', " +
          "`language` TEXT NOT NULL DEFAULT '', " +
          "`homepage` TEXT NOT NULL DEFAULT '', " +
          "`status` TEXT NOT NULL DEFAULT '', " +
          "`rating` REAL NOT NULL DEFAULT -1, " +
          "`votes` INTEGER NOT NULL DEFAULT -1, " +
          "`comment_count` INTEGER NOT NULL DEFAULT -1, " +
          "`genres` TEXT NOT NULL DEFAULT '', " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1)",
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_discover` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL DEFAULT -1, " +
          "`created_at` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE INDEX index_discover_movies_id_trakt ON movies_discover(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_images` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_tmdb` INTEGER NOT NULL DEFAULT -1, " +
          "`type` TEXT NOT NULL DEFAULT '', " +
          "`file_url` TEXT NOT NULL DEFAULT '', " +
          "`source` TEXT NOT NULL DEFAULT 'tmdb')",
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_translations` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, `title` TEXT NOT NULL, " +
          "`language` TEXT NOT NULL, " +
          "`overview` TEXT NOT NULL, " +
          "`created_at` INTEGER NOT NULL, " +
          "`updated_at` INTEGER NOT NULL, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE UNIQUE INDEX index_movies_translations_id_trakt ON movies_translations(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_movies_translations_log` (" +
          "`id_movie_trakt` INTEGER PRIMARY KEY NOT NULL, " +
          "`synced_at` INTEGER NOT NULL)",
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_related` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL  DEFAULT -1, " +
          "`id_trakt_related_movie` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt_related_movie`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE INDEX index_movies_related_id_trakt ON movies_related(id_trakt_related_movie)")

      database.execSQL("ALTER TABLE actors ADD COLUMN id_tmdb_movie INTEGER NOT NULL DEFAULT -1")
      database.execSQL("ALTER TABLE actors ADD COLUMN id_tmdb INTEGER NOT NULL DEFAULT -1")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_my_movies` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL DEFAULT -1, " +
          "`created_at` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE INDEX index_movies_my_movies_id_trakt ON movies_my_movies(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `movies_see_later` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL DEFAULT -1, " +
          "`created_at` INTEGER NOT NULL DEFAULT -1, " +
          "`updated_at` INTEGER NOT NULL DEFAULT -1, " +
          "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
      )
      database.execSQL("CREATE INDEX index_movies_see_later_id_trakt ON movies_see_later(id_trakt)")

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_movies_log` (" +
          "`id_movie_trakt` INTEGER PRIMARY KEY NOT NULL DEFAULT -1, " +
          "`synced_at` INTEGER NOT NULL DEFAULT 0)",
      )

      database.execSQL(
        "CREATE TABLE IF NOT EXISTS `sync_trakt_log` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`id_trakt` INTEGER NOT NULL, " +
          "`type` TEXT NOT NULL, " +
          "`synced_at` INTEGER NOT NULL)",
      )
      database.execSQL("CREATE INDEX index_sync_trakt_log_id_trakt ON sync_trakt_log(id_trakt)")
      database.execSQL("CREATE INDEX index_sync_trakt_log_type ON sync_trakt_log(type)")
      database.execSQL("CREATE UNIQUE INDEX index_sync_trakt_log_id_trakt_type ON sync_trakt_log(id_trakt, type)")
    }
  }

  private val migration16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN show_collection_shows INTEGER NOT NULL DEFAULT 1")
      database.execSQL("ALTER TABLE settings ADD COLUMN show_collection_movies INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val migration17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN widgets_show_label INTEGER NOT NULL DEFAULT 1")
    }
  }

  private val migration18 = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE actors ADD COLUMN id_tmdb_show INTEGER NOT NULL DEFAULT -1")
        execSQL("DELETE FROM actors")

        execSQL("ALTER TABLE shows_images ADD COLUMN id_tmdb INTEGER NOT NULL DEFAULT -1")
        execSQL("DELETE FROM shows_images WHERE source = 'tvdb' OR family = 'movie'")

        execSQL("CREATE INDEX index_shows_images_tmdb_id_type_family ON shows_images(id_tmdb, type, family)")
        execSQL("CREATE INDEX index_movies_images_tmdb_id_type ON movies_images(id_tmdb, type)")
      }
    }
  }

  private val migration19 = object : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE settings ADD COLUMN my_movies_recent_is_enabled INTEGER NOT NULL DEFAULT 1")
      }
    }
  }

  private val migration20 = object : Migration(19, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `custom_images` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`family` TEXT NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`file_url` TEXT NOT NULL)",
        )
        execSQL("CREATE INDEX index_custom_images_trakt_id_family_type ON custom_images(id_trakt, family, type)")
      }
    }
  }

  private val migration21 = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE settings ADD COLUMN quick_rate_enabled INTEGER NOT NULL DEFAULT 0")
      }
    }
  }

  private val migration22 = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE shows ADD COLUMN created_at INTEGER NOT NULL DEFAULT -1")
        val cursor = database.query("SELECT id_trakt, updated_at FROM shows")
        while (cursor.moveToNext()) {
          val id = cursor.getLong(cursor.getColumnIndexOrThrow("id_trakt"))
          val updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
          execSQL("UPDATE shows SET created_at = $updatedAt WHERE id_trakt == $id")
        }
      }
    }
  }

  private val migration23 = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `custom_lists` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER, " +
            "`id_slug` TEXT NOT NULL, " +
            "`name` TEXT NOT NULL, " +
            "`description` TEXT, " +
            "`privacy` TEXT NOT NULL, " +
            "`display_numbers` INTEGER NOT NULL, " +
            "`allow_comments` INTEGER NOT NULL, " +
            "`sort_by` TEXT NOT NULL, " +
            "`sort_how` TEXT NOT NULL, " +
            "`sort_by_local` TEXT NOT NULL, " +
            "`sort_how_local` TEXT NOT NULL, " +
            "`filter_type_local` TEXT NOT NULL, " +
            "`item_count` INTEGER NOT NULL, " +
            "`comment_count` INTEGER NOT NULL, " +
            "`likes` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL" +
            ")",
        )
        execSQL("CREATE UNIQUE INDEX index_custom_lists_id_trakt ON custom_lists(id_trakt)")
        execSQL("ALTER TABLE settings ADD COLUMN lists_sort_by TEXT NOT NULL DEFAULT 'DATE_UPDATED'")
      }
    }
  }

  private val migration24 = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `custom_list_item` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_list` INTEGER NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`rank` INTEGER NOT NULL, " +
            "`listed_at` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_list`) REFERENCES `custom_lists`(`id`) ON DELETE CASCADE" +
            ")",
        )
        execSQL("CREATE INDEX index_custom_list_item_id_list ON custom_list_item(id_list)")
        execSQL("CREATE INDEX index_custom_list_item_id_trakt_type ON custom_list_item(id_trakt, type)")
        execSQL(
          "CREATE UNIQUE INDEX " +
            "index_custom_list_item_id_list_id_trakt_type ON custom_list_item(id_list, id_trakt, type)",
        )

        execSQL("ALTER TABLE trakt_sync_queue ADD COLUMN id_list INTEGER")
        execSQL("ALTER TABLE trakt_sync_queue ADD COLUMN operation TEXT NOT NULL DEFAULT ''")
      }
    }
  }

  private val migration25 = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `news` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_news` TEXT NOT NULL, " +
            "`title` TEXT NOT NULL, " +
            "`url` TEXT NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`image` TEXT, " +
            "`score` INTEGER NOT NULL, " +
            "`dated_at` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL " +
            ")",
        )

        execSQL("ALTER TABLE user ADD COLUMN reddit_token TEXT NOT NULL DEFAULT ''")
        execSQL("ALTER TABLE user ADD COLUMN reddit_token_timestamp INTEGER NOT NULL DEFAULT 0")
      }
    }
  }

  private val migration26 = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE settings ADD COLUMN progress_upcoming_enabled INTEGER NOT NULL DEFAULT 1")
      }
    }
  }

  private val migration27 = object : Migration(26, 27) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `movies_ratings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`trakt` TEXT, " +
            "`imdb` TEXT, " +
            "`metascore` TEXT, " +
            "`rotten_tomatoes` TEXT, " +
            "`rotten_tomatoes_url` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE UNIQUE INDEX index_movies_ratings_id_trakt ON movies_ratings(id_trakt)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `shows_ratings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`trakt` TEXT, " +
            "`imdb` TEXT, " +
            "`metascore` TEXT, " +
            "`rotten_tomatoes` TEXT, " +
            "`rotten_tomatoes_url` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE UNIQUE INDEX index_shows_ratings_id_trakt ON shows_ratings(id_trakt)")
      }
    }
  }

  private val migration28 = object : Migration(27, 28) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `movies_streamings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`id_tmdb` INTEGER NOT NULL, " +
            "`type` TEXT, " +
            "`provider_id` INTEGER, " +
            "`provider_name` TEXT, " +
            "`display_priority` INTEGER, " +
            "`logo_path` TEXT, " +
            "`link` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE INDEX index_movies_streamings_id_trakt ON movies_streamings(id_trakt)")
        execSQL("CREATE INDEX index_movies_streamings_id_tmdb ON movies_streamings(id_tmdb)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `shows_streamings` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`id_tmdb` INTEGER NOT NULL, " +
            "`type` TEXT, " +
            "`provider_id` INTEGER, " +
            "`provider_name` TEXT, " +
            "`display_priority` INTEGER, " +
            "`logo_path` TEXT, " +
            "`link` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE INDEX index_shows_streamings_id_trakt ON shows_streamings(id_trakt)")
        execSQL("CREATE INDEX index_shows_streamings_id_tmdb ON shows_streamings(id_tmdb)")
      }
    }
  }

  private val migration29 = object : Migration(28, 29) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE movies ADD COLUMN created_at INTEGER NOT NULL DEFAULT -1")
        val cursor = database.query("SELECT id_trakt, updated_at FROM movies")
        while (cursor.moveToNext()) {
          val id = cursor.getLong(cursor.getColumnIndexOrThrow("id_trakt"))
          val updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
          execSQL("UPDATE movies SET created_at = $updatedAt WHERE id_trakt == $id")
        }

        execSQL(
          "CREATE TABLE IF NOT EXISTS `movies_archive` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE UNIQUE INDEX index_movies_archive_id_trakt ON movies_archive(id_trakt)")
      }
    }
  }

  private val migration30 = object : Migration(29, 30) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("DROP TABLE actors")
        execSQL(
          "CREATE TABLE IF NOT EXISTS `people` (" +
            "`id_tmdb` INTEGER PRIMARY KEY NOT NULL, " +
            "`id_trakt` INTEGER, " +
            "`id_imdb` TEXT, " +
            "`name` TEXT NOT NULL, " +
            "`department` TEXT NOT NULL, " +
            "`biography` TEXT, " +
            "`biography_translation` TEXT, " +
            "`birthday` TEXT, " +
            "`character` TEXT, " +
            "`job` TEXT, " +
            "`episodes_count` INTEGER, " +
            "`birthplace` TEXT, " +
            "`deathday` TEXT, " +
            "`image_path` TEXT, " +
            "`homepage` TEXT, " +
            "`created_at` INTEGER NOT NULL, " +
            "`details_updated_at` INTEGER, " +
            "`updated_at` INTEGER NOT NULL)",
        )
        execSQL("CREATE INDEX index_people_id_trakt ON people(id_trakt)")
        execSQL("CREATE UNIQUE INDEX index_people_id_tmdb ON people(id_tmdb)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `people_shows_movies` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_tmdb_person` INTEGER NOT NULL, " +
            "`mode` TEXT NOT NULL, " +
            "`department` TEXT NOT NULL, " +
            "`character` TEXT, " +
            "`job` TEXT, " +
            "`episodes_count` INTEGER NOT NULL, " +
            "`id_trakt_show` INTEGER, " +
            "`id_trakt_movie` INTEGER, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_tmdb_person`) REFERENCES `people`(`id_tmdb`) ON DELETE CASCADE)",
        )
        execSQL("CREATE INDEX index_people_shows_movies_id_show_mode ON people_shows_movies(id_trakt_show, mode)")
        execSQL("CREATE INDEX index_people_shows_movies_id_movie_mode ON people_shows_movies(id_trakt_movie, mode)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `people_credits` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt_person` INTEGER NOT NULL, " +
            "`id_trakt_show` INTEGER, " +
            "`id_trakt_movie` INTEGER, " +
            "`type` TEXT NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt_show`) REFERENCES `shows`(`id_trakt`) ON DELETE CASCADE, " +
            "FOREIGN KEY(`id_trakt_movie`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE INDEX index_people_credits_id_person ON people_credits(id_trakt_person)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `people_images` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_tmdb` INTEGER NOT NULL, " +
            "`file_path` TEXT NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL)",
        )
        execSQL("CREATE INDEX index_people_images_id_person ON people_images(id_tmdb)")
      }
    }
  }

  private val migration31 = object : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `ratings` (" +
            "`id_trakt` INTEGER NOT NULL, " +
            "`type` TEXT NOT NULL, " +
            "`rating` INTEGER NOT NULL, " +
            "`season_number` INTEGER, " +
            "`episode_number` INTEGER, " +
            "`rated_at` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "PRIMARY KEY (id_trakt, type))",
        )
        execSQL("CREATE INDEX index_ratings_id_trakt_type ON ratings(id_trakt, type)")

        execSQL("ALTER TABLE seasons ADD COLUMN rating REAL")

        execSQL("CREATE INDEX index_people_credits_show ON people_credits(id_trakt_show)")
        execSQL("CREATE INDEX index_people_credits_movies ON people_credits(id_trakt_movie)")
        execSQL("CREATE INDEX index_people_shows_movies_tmdb_person ON people_shows_movies(id_tmdb_person)")
      }
    }
  }

  private val migration32 = object : Migration(31, 32) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE episodes ADD COLUMN episode_number_abs INTEGER")
    }
  }

  private val migration33 = object : Migration(32, 33) {
    override fun migrate(database: SupportSQLiteDatabase) {
      val preferences = context.applicationContext.getSharedPreferences("PREFERENCES_NETWORK", MODE_PRIVATE)
      val preferencesEditor = preferences.edit()
      val cursor = database.query("SELECT trakt_token, trakt_refresh_token FROM user")
      while (cursor.moveToNext()) {
        val accessToken = cursor.getString(cursor.getColumnIndexOrThrow("trakt_token"))
        val refreshToken = cursor.getString(cursor.getColumnIndexOrThrow("trakt_refresh_token"))
        accessToken?.let { preferencesEditor.putString("TRAKT_ACCESS_TOKEN", it) }
        refreshToken?.let { preferencesEditor.putString("TRAKT_REFRESH_TOKEN", it) }
      }
      preferencesEditor.apply()
    }
  }

  private val migration34 = object : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE episodes ADD COLUMN last_watched_at INTEGER")
        execSQL("ALTER TABLE shows_my_shows ADD COLUMN last_watched_at INTEGER")
        val cursor = database.query("SELECT id_trakt, updated_at FROM shows_my_shows")
        while (cursor.moveToNext()) {
          val idShow = cursor.getLong(cursor.getColumnIndexOrThrow("id_trakt"))
          val updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))

          val epCursor = database.query(
            "SELECT season_number, episode_number FROM episodes WHERE id_show_trakt == $idShow AND is_watched == 1 " +
              "ORDER BY season_number DESC, episode_number DESC LIMIT 1",
          )
          while (epCursor.moveToNext()) {
            val episodeNumber = epCursor.getLong(epCursor.getColumnIndexOrThrow("episode_number"))
            val seasonNumber = epCursor.getLong(epCursor.getColumnIndexOrThrow("season_number"))
            if (updatedAt > 0 && episodeNumber > 0 && seasonNumber > 0) {
              execSQL(
                "UPDATE episodes SET last_watched_at = $updatedAt " +
                  "WHERE id_show_trakt == $idShow " +
                  "AND is_watched == 1 " +
                  "AND episode_number == $episodeNumber AND season_number == $seasonNumber",
              )
              execSQL("UPDATE shows_my_shows SET last_watched_at = $updatedAt WHERE id_trakt == $idShow")
            }
          }
        }
      }
    }
  }

  private val migration35 = object : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("ALTER TABLE settings ADD COLUMN discover_filter_networks TEXT NOT NULL DEFAULT ''")
    }
  }

  private val migration36 = object : Migration(35, 36) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL(
          "CREATE TABLE IF NOT EXISTS `movies_collections` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`id_trakt_movie` INTEGER NOT NULL, " +
            "`name` TEXT NOT NULL, " +
            "`description` TEXT NOT NULL, " +
            "`item_count` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt_movie`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE INDEX index_movies_collections_id_trakt ON movies_collections(id_trakt)")
        execSQL("CREATE INDEX index_movies_collections_id_trakt_movie ON movies_collections(id_trakt_movie)")

        execSQL(
          "CREATE TABLE IF NOT EXISTS `movies_collections_items` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`id_trakt` INTEGER NOT NULL, " +
            "`id_trakt_collection` INTEGER NOT NULL, " +
            "`rank` INTEGER NOT NULL, " +
            "`created_at` INTEGER NOT NULL, " +
            "`updated_at` INTEGER NOT NULL, " +
            "FOREIGN KEY(`id_trakt`) REFERENCES `movies`(`id_trakt`) ON DELETE CASCADE)",
        )
        execSQL("CREATE INDEX index_movies_collections_items_id_trakt ON movies_collections_items(id_trakt)")
        execSQL(
          "CREATE INDEX " +
            "index_movies_collections_items_id_trakt_collection ON movies_collections_items(id_trakt_collection)",
        )
      }
    }
  }

  private val migration37 = object : Migration(36, 37) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        val cursor = database.query("SELECT my_shows_recent_is_enabled, my_movies_recent_is_enabled FROM settings")
        while (cursor.moveToNext()) {
          val myShowsRecentsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("my_shows_recent_is_enabled"))
          val myMoviesRecentsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("my_movies_recent_is_enabled"))

          if (myShowsRecentsEnabled != 1 && myMoviesRecentsEnabled != 1) {
            execSQL("UPDATE settings SET my_shows_recent_amount = 0")
          }
        }
      }
    }
  }

  private val migration38 = object : Migration(37, 38) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        val cursor = database.query("SELECT progress_upcoming_enabled FROM settings")
        while (cursor.moveToNext()) {
          val isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("progress_upcoming_enabled"))
          if (isEnabled != 1) {
            val preferences = context.applicationContext.getSharedPreferences("PREFERENCES_MISC", MODE_PRIVATE)
            preferences.edit().apply {
              putLong("PROGRESS_UPCOMING_DAYS", 0)
              apply()
            }
          }
        }
      }
    }
  }

  private val migration39 = object : Migration(38, 39) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("DROP TABLE IF EXISTS news")
      }
    }
  }

  private val migration40 = object : Migration(39, 40) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("DROP TABLE IF EXISTS custom_images")
      }
    }
  }

  private val migration41 = object : Migration(40, 41) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE episodes ADD COLUMN last_exported_at INTEGER")
      }
    }
  }

  private val migration42 = object : Migration(41, 42) {
    override fun migrate(database: SupportSQLiteDatabase) {
      with(database) {
        execSQL("ALTER TABLE movies ADD COLUMN original_release TEXT NOT NULL DEFAULT ''")
        execSQL("ALTER TABLE movies ADD COLUMN current_country TEXT NOT NULL DEFAULT ''")
      }
    }
  }

  fun getAll() = listOf(
    migration2,
    migration3,
    migration4,
    migration5,
    migration6,
    migration7,
    migration8,
    migration9,
    migration10,
    migration11,
    migration12,
    migration13,
    migration14,
    migration15,
    migration16,
    migration17,
    migration18,
    migration19,
    migration20,
    migration21,
    migration22,
    migration23,
    migration24,
    migration25,
    migration26,
    migration27,
    migration28,
    migration29,
    migration30,
    migration31,
    migration32,
    migration33,
    migration34,
    migration35,
    migration36,
    migration37,
    migration38,
    migration39,
    migration40,
    migration41,
    migration42
  )
}
