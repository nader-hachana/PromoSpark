CREATE KEYSPACE IF NOT EXISTS sales WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 1 };

USE sales;

CREATE TABLE IF NOT EXISTS db_sales (
    prod_purch int,
    store_id int,
    purch_week int,
    promo_cat int,
    baseline double,
    promo_discount double,
    weekly_sales bigint,
    lift_unit double,
    lift_percentage double,
    total_sales_unit_promo_prod bigint,
    PRIMARY KEY (prod_purch, store_id, purch_week)
    );