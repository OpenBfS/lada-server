INSERT INTO master.status_ord_mp VALUES ((SELECT MAX(id)+1 FROM master.status_ord_mp), 14, 1);
