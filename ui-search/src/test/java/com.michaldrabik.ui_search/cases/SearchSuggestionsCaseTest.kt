package com.michaldrabik.ui_search.cases

import com.google.common.truth.Truth.assertThat
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.dao.MoviesDao
import com.michaldrabik.data_local.database.dao.ShowsDao
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.locale.AppCountry
import com.michaldrabik.ui_model.locale.AppLanguage
import com.michaldrabik.ui_model.locale.AppLocale
import com.michaldrabik.ui_search.BaseMockTest
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchSuggestionsCaseTest : BaseMockTest() {

  companion object {
    val TEST_LOCALE = AppLocale.fromCode("pt_br")
  }

  @RelaxedMockK lateinit var database: LocalDataSource
  @RelaxedMockK lateinit var showsDao: ShowsDao
  @RelaxedMockK lateinit var moviesDao: MoviesDao
  @RelaxedMockK lateinit var mappers: Mappers
  @RelaxedMockK lateinit var showsRepository: ShowsRepository
  @RelaxedMockK lateinit var moviesRepository: MoviesRepository
  @RelaxedMockK lateinit var settingsRepository: SettingsRepository
  @RelaxedMockK lateinit var translationsRepository: TranslationsRepository
  @RelaxedMockK lateinit var showImagesProvider: ShowImagesProvider
  @RelaxedMockK lateinit var movieImagesProvider: MovieImagesProvider

  private lateinit var SUT: SearchSuggestionsCase

  @Before
  override fun setUp() {
    super.setUp()

    coEvery { settingsRepository.isMoviesEnabled } returns true
    coEvery { translationsRepository.getLocale() } returns TEST_LOCALE
    coEvery { database.shows } returns showsDao
    coEvery { database.movies } returns moviesDao

    SUT = SearchSuggestionsCase(
      dispatchers = testDispatchers,
      localSource = database,
      mappers = mappers,
      showsRepository = showsRepository,
      moviesRepository = moviesRepository,
      translationsRepository = translationsRepository,
      settingsRepository = settingsRepository,
      showsImagesProvider = showImagesProvider,
      moviesImagesProvider = movieImagesProvider
    )
  }

  @After
  fun tearDown() {
    clearAllMocks()
  }

  @Test
  fun `Should skip preload local shows cache if already loaded`() = runTest {
    SUT.preloadCache() // Initial preload. Db data should be loaded
    SUT.preloadCache() // Further preload. Db data should not be loaded
    coVerify(exactly = 1) { showsDao.getAll() }
  }

  @Test
  fun `Should skip preload local movies cache if already loaded`() = runTest {
    SUT.preloadCache() // Initial preload. Db data should be loaded
    SUT.preloadCache() // Further preload. Db data should not be loaded
    coVerify(exactly = 1) { moviesDao.getAll() }
  }

  @Test
  fun `Should skip preload local movies cache if movies disabled`() = runTest {
    coEvery { settingsRepository.isMoviesEnabled } returns false

    SUT.preloadCache()
    coVerify(exactly = 0) { moviesDao.getAll() }
  }

  @Test
  fun `Should skip preload local shows translations cache if default language`() = runTest {
    SUT.preloadCache()
    coVerify(exactly = 0) { translationsRepository.loadAllShowsLocal(any()) }
  }

  @Test
  fun `Should skip preload local movies translations cache if default language`() = runTest {
    SUT.preloadCache()
    coVerify(exactly = 0) { translationsRepository.loadAllMoviesLocal(any()) }
  }

  @Test
  fun `Should skip preload local movies translations cache if not default language but movies are disabled`() =
    runTest {
      coEvery { translationsRepository.getLocale() } returns TEST_LOCALE
      coEvery { settingsRepository.isMoviesEnabled } returns false

      SUT.preloadCache()
      coVerify(exactly = 0) { translationsRepository.loadAllMoviesLocal(any()) }
    }

  @Test
  fun `Should preload local cache`() = runTest {
    SUT.preloadCache()
    coVerify(exactly = 1) { showsDao.getAll() }
    coVerify(exactly = 1) { moviesDao.getAll() }
  }

  @Test
  fun `Should preload local translations cache`() = runTest {
    coEvery { translationsRepository.getLocale() } returns TEST_LOCALE

    SUT.preloadCache()

    coVerify(exactly = 1) { translationsRepository.loadAllShowsLocal(TEST_LOCALE) }
    coVerify(exactly = 1) { translationsRepository.loadAllMoviesLocal(TEST_LOCALE)}
  }

  @Test
  fun `Should return empty list if query is blank`() = runTest {
    val result = SUT.loadSuggestions("   ")

    assertThat(result).isEmpty()
    coVerify(exactly = 0) { showsDao.getAll() }
    coVerify(exactly = 0) { moviesDao.getAll() }
  }

  @Test
  fun `Should clear local caches properly`() = runTest {
    coEvery { translationsRepository.getLocale() } returns TEST_LOCALE

    SUT.preloadCache()
    SUT.clearCache()
    SUT.preloadCache()

    coVerify(exactly = 2) { showsDao.getAll() }
    coVerify(exactly = 2) { moviesDao.getAll() }
    coVerify(exactly = 2) { translationsRepository.loadAllShowsLocal(TEST_LOCALE) }
    coVerify(exactly = 2) { translationsRepository.loadAllMoviesLocal(TEST_LOCALE) }
  }
}
