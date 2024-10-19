# This is a sample Python script.

import random
import faker
from datetime import date
import bcrypt

# Press Maj+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.




def print_hi(name):
    # Initialisation de faker pour générer des données réalistes
    fake = faker.Faker('fr_FR')

    usernames_set = set()
    no_unique_jeu_set = set()

    def generate_unique_number(length):
        while True:
            # Générer un nombre de la longueur spécifiée
            number = str(fake.random_number(digits=length, fix_len=True))
            # Vérifier si le nombre est déjà utilisé
            if number not in no_unique_jeu_set:
                # Ajouter le nombre à l'ensemble des nombres générés
                no_unique_jeu_set.add(number)
                return number

    def generate_unique_username():
        while True:
            # Générer un username de 12 chiffres
            username = str(fake.random_number(digits=12, fix_len=True))
            # Vérifier si le username est déjà utilisé
            if username not in usernames_set:
                # Ajouter le username à l'ensemble des usernames générés
                usernames_set.add(username)
                return username

    # Nombre d'insertions à générer
    n = 6000

    # Générer des requêtes SQL pour les items, books, magazines, et board_games
    with open('insert_data.sql', 'w') as f:
        f.write("-- Insertion massive de données\n")

        # Requêtes pour la table items
        f.write("-- Insertion dans items\n")
        for i in range(1, n + 1):
            category_id = random.randint(1, 4)
            publisher_id = random.randint(1, 4)
            supplier_id = random.randint(1, 4)
            item_type = ['book', 'magazine', 'board_game'][i % 3]
            price = round(random.uniform(5, 100), 2)
            name = fake.sentence(nb_words=3).replace("'", "''")  # Gérer les apostrophes

            # URL ou NULL une fois sur deux
            link = f"'{fake.url()}'" if i % 2 == 0 else "NULL"

            f.write(
                f"INSERT INTO items (id, title, category, publisher_id, supplier_id, type, value, link) "
                f"VALUES ({i}, '{name}', {category_id}, {publisher_id}, {supplier_id}, '{item_type}', {price}, {link});\n")

        f.write("\n")

        # Requêtes pour la table books (seulement pour les items de type 'book')
        f.write("-- Insertion dans books\n")
        for i in range(1, n + 1):
            if i % 3 == 0:  # Disons qu'un tiers des items sont des livres
                isbn = fake.isbn13()
                author = fake.name().replace("'", "''")
                publication_year = fake.date_between(start_date=date(1900,1,1),end_date=date(2023,12,31))
                f.write(f"INSERT INTO books (item_id, isbn, author, publication_date) "
                        f"VALUES ({i}, '{isbn}', '{author}', '{publication_year}');\n")

        # Requêtes pour la table magazines (seulement pour les items de type 'magazine')
        f.write("-- Insertion dans magazines\n")
        for i in range(1, n + 1):
            if i % 3 == 1:  # Disons qu'un autre tiers des items sont des magazines
                issue_number = fake.isbn13()
                publication_date = fake.date_between(start_date='-10y', end_date='today')
                month = fake.month()
                f.write(f"INSERT INTO magazines (item_id, isni, publication_date, month) "
                        f"VALUES ({i}, '{issue_number}', '{publication_date}', '{month}' );\n")

        f.write("\n")

        # Requêtes pour la table board_games (seulement pour les items de type 'board_game')
        f.write("-- Insertion dans board_games\n")
        for i in range(1, n + 1):
            if i % 3 == 2:  # Le dernier tiers des items sont des jeux de société
                nbr_pieces = random.randint(1, 1000)
                recommended_age = random.randint(5, 90)
                text = fake.text(255).replace("'", "''")

                gtin = generate_unique_number(random.choice([8, 12, 13, 14]))

                f.write(
                    f"INSERT INTO board_games (item_id, number_of_pieces, recommended_age, game_rules, gtin) "
                    f"VALUES ({i}, {nbr_pieces}, {recommended_age}, '{text}', '{gtin}');\n")

        f.write("\n")

        # Requêtes pour la table copies
        f.write("-- Insertion dans copies\n")
        copy_id = 1  # Compteur pour les IDs de copies
        statuses = ['available', 'borrowed', 'unavailable', 'deleted']

        for i in range(1, n + 1):
            num_copies = random.randint(1, 2)  # 1 à 2 copies par item

            for _ in range(num_copies):
                date_achat = fake.date_between(start_date=date(1960, 1, 1), end_date=date(2023, 12, 31))
                status = random.choice(statuses)
                price = round(random.uniform(5, 100), 2)
                f.write(f"INSERT INTO copies (id, item_id, status, acquisition_date, price) VALUES ({copy_id}, {i}, '{status}', '{date_achat}', {price});\n")
                copy_id += 1

        f.write("\n")

        # Requêtes pour la table users
        f.write("-- Insertion dans users\n")
        user_id = 4  # Compteur pour les IDs d'utilisateurs

        # Générer 90 membres
        for _ in range(90):
            username = generate_unique_username()
            password = bcrypt.hashpw('password'.encode('utf-8'), bcrypt.gensalt())
            password = password.decode('utf-8')

            first_name = fake.first_name().replace("'", "''")
            last_name = fake.last_name().replace("'", "''")
            email = fake.email().replace("'", "''")
            phone_number = fake.phone_number().replace("'", "''")
            status = 'active'
            role = 1
            date_birth = fake.date_between(start_date=date(1960, 1, 1), end_date=date(2000, 12, 31))
            f.write(
                f"INSERT INTO users (id, username, password, first_name, last_name, email, phone_number, status, role_id, is_child, date_of_birth) "
                f"VALUES ({user_id}, '{username}', '{password}', '{first_name}', '{last_name}', '{email}', '{phone_number}', '{status}', {role}, 0, '{date_birth}');\n")
            user_id += 1

        # Générer 10 bénévoles
        for _ in range(10):
            username = generate_unique_username()
            password = bcrypt.hashpw('password'.encode('utf-8'), bcrypt.gensalt())
            password = password.decode('utf-8')
            first_name = fake.first_name().replace("'", "''")
            last_name = fake.last_name().replace("'", "''")
            email = fake.email().replace("'", "''")
            phone_number = fake.phone_number().replace("'", "''")
            status = 'actif'
            role = 2
            f.write(
                f"INSERT INTO users (id, username, password, first_name, last_name, email, phone_number, status, role_id, is_child, date_of_birth) "
                f"VALUES ({user_id}, '{username}', '{password}', '{first_name}', '{last_name}', '{email}', '{phone_number}', '{status}', {role}, 0, '{date_birth}');\n")
            user_id += 1

    print("Les données de test ont été générées avec succès dans 'insert_data.sql'.")


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    print_hi('PyCharm')

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
