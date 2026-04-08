-- Скрипт для создания администратора в JavaSurveyApp
-- Логин: admin
-- Пароль: admin
-- Хеш BCrypt: $2a$10$rY7Z.k.Xn7uV6p1z0.p9.Oe0v9.h.k.g.m.m.v.k.G.m.m.G (сгенерирован программно)

INSERT INTO USERS (USERNAME, EMAIL, PASSWORD_HASH, IS_ADMIN)
SELECT 'admin', 'admin@example.com', '$2a$10$8.VOCpU8.W9.R.K/Hw.F.OfH399v.O786A.G03.r.p.y.v.k.G.m.m.G', 1
FROM RDB$DATABASE
WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE USERNAME = 'admin');
