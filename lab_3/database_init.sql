/* закомментировал, чтобы VS не засорял список ошибок ложноположительными ошибками из этого файла
CREATE TYPE trash_type AS ENUM ('Пластик', 'Бумага', 'Картон', 'Стекло', 'Метал');

CREATE TABLE trash_containers (
    id SERIAL PRIMARY KEY,
    city TEXT,
    street TEXT,
    house INT,
    allowed_waste trash_type[]
);

INSERT INTO trash_containers (city, street, house, allowed_waste) VALUES
('Москва', 'Ленинградский проспект', 78, ARRAY['Пластик','Бумага']::trash_type[]),
('Санкт-Петербург', 'Невский проспект', 69, ARRAY['Картон','Стекло']::trash_type[]),
('Екатеринбург', 'Верх-Исетский бульвар', 12, ARRAY['Метал']::trash_type[]);
*/