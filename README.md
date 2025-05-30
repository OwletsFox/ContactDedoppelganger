# ContactDedoppelganger
Вариант 3 тестового задания для стажировки. Приложение для просмотра контактов с возможностью удалить повторяющиеся.

## Описание
Приложение отображает список контактов, хранящихся на устройстве, и позволяет пользователю удалять полностью совпадающие записи через AIDL-сервис.

## Функционал
- Отображение списка всех контактов (имя + первый номер)
- Группировка по первой букве имени с секциями в списке
- Поиск и удаление дублирующихся контактов
- Индикация процесса загрузки и уведомления об успешном/неудачном удалении

## Архитектура
- **Чистая архитектура + SOLID + MVVM**
- Слой данных: AIDL-сервис, репозитории доступа к сервису, DTO, mappers
- Доменный слой: UseCases, Доменные модели
- Слой UI: Activity, ViewModel, adapters, модели представления
- DI: Dagger Hilt

## Стек технологий
- Kotlin
- AndroidX Core, AppCompat, Material, ConstraintLayout
- Lifecycle (ViewModel, LiveData)
- RecyclerView + ConcatAdapter
- Kotlin Coroutines
- Dagger Hilt for DI
- AIDL + ContentResolver
- Junit 4 + Mokito (Unit-тесты)

## Unit-тесты
Покрытие бизнес-логики Unit-тестами на JUnit4 + Mockito:
- **domain/usecase**  
  - `GetContactsUseCaseTest`  
  - `RemoveDuplicateContactsUseCaseTest`  
- **ui/viewmodel**  
  - `MainViewModelTest`

Применяемые техники тест-дизайна: Классы эквивалентности / Прогнозирование ошибок
### Запуск тестов
```bash
./gradlew test
