# Fakezon

Fakezon is an e-commerce platform inspired by Amazon, designed for educational and demonstration purposes. The system supports user registration, store and product management, auctions, offers, discounts, and role-based permissions for owners and managers.

## Features

- **User Management:** Registration, login, roles (owner, manager, customer).
- **Store Management:** Create, edit, and close stores; assign owners and managers.
- **Product Management:** Add, edit, remove products; support for auctions and offers.
- **Discounts & Policies:** Flexible discount system (AND, OR, XOR, Condition discounts) and purchase policies.
- **Role-Based Permissions:** Fine-grained permissions for store managers and owners.
- **Messaging:** Communication between users and store managers/owners.
- **Auctions & Offers:** Place bids, handle counter-offers, and manage auction lifecycle.

## Getting Started

### Initialization

**Important:**  
The system is initialized using the `TempDataLoader` class. This class loads initial data such as users, stores, products, and roles for testing and demonstration.  
Make sure to run or configure `TempDataLoader` before starting the application to ensure the system has the necessary data to operate.

### Prerequisites

- Java 17 or higher
- Maven or Gradle (for dependency management)
- (Optional) Spring Boot for running the application

### Running the Project

1. Clone the repository.
2. Build the project using Maven or Gradle.
3. Ensure `TempDataLoader` is executed at startup (check your main method or Spring Boot configuration).
4. Run the application.

### Running Tests

Unit tests are located under `src/test/java/UnitTesting/`.  
You can run all tests using your IDE or with Maven:

```sh
mvn test
```

## Project Structure

- `DomainLayer/Model/` — Core business logic (stores, products, discounts, policies, etc.)
- `ApplicationLayer/DTO/` — Data transfer objects for API and UI communication
- `src/test/java/UnitTesting/` — Unit tests for all major components
- `TempDataLoader` — Loads initial data for the system

## Notes

- This project is for academic and demonstration purposes.
- The codebase is modular and extensible for adding new features or policies.
- For any issues or questions, please refer to the code comments or contact the maintainers.
