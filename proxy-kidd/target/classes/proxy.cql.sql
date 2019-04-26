--Create keyspace
CREATE KEYSPACE IF NOT EXISTS proxy WITH REPLICATION = {'class': 'SimpleStrategy','replication_factor':1};

use proxy;

--Create table
CREATE TABLE IF NOT EXISTS proxy (
  ip text PRIMARY KEY,
  port int,
  protocol int,
  type int,
  location text,
  update_time bigint
);