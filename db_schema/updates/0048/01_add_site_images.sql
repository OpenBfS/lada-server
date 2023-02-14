CREATE TABLE master.site_image(
    id SERIAL PRIMARY KEY,
    img BYTEA NOT NULL
);

ALTER TABLE master.site
    ADD COLUMN img INT REFERENCES master.site_image,
    ADD COLUMN map INT REFERENCES master.site_image;
