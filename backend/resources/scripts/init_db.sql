CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE chart_node (
	id uuid NOT NULL DEFAULT uuid_generate_v4(),
	parentid uuid NULL,
	title varchar NOT NULL,
	"level" int4 NOT NULL,
	CONSTRAINT chart_node_pk PRIMARY KEY (id),
	CONSTRAINT chart_node_fk FOREIGN KEY (parentid) REFERENCES chart_node(id)
);