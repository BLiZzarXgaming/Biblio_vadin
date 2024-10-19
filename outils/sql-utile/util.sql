
-- changer les caractères spéciaux en caractères normaux
UPDATE nom_de_table
SET nom_du_champ = REPLACE(nom_du_champ, '�', 'a')
WHERE nom_du_champ LIKE '%�%';

-- changer la date des événements pour les tests
UPDATE `availabilities`
SET `date` = STR_TO_DATE(
        CONCAT(
            YEAR(CURDATE()), '-',
            LPAD(MONTH(CURDATE()), 2, '0'), '-',
            LPAD(LEAST(DAY(`date`), DAY(LAST_DAY(CURDATE()))), 2, '0')
        ),
        '%Y-%m-%d'
);

-- donne la date du jour
SELECT CURDATE();