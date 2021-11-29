package com.michaldrabik.ui_people.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_model.Person
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsLoadCase @Inject constructor(
  private val peopleRepository: PeopleRepository,
  private val dateFormatProvider: DateFormatProvider
) {

  suspend fun loadDetails(person: Person) =
    peopleRepository.loadDetails(person)
      .copy(character = person.character)

  fun loadDateFormat() = dateFormatProvider.loadShortDayFormat()
}
