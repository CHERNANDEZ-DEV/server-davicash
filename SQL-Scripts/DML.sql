--==================================================================
-- 5. INSERTS INICIALES
--==================================================================
-- 1) Habilita la extensión para generar UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO
    public.entities (
        id,
        account_bank,
        authentication_mode,
        code,
        email,
        entity_type,
        name,
        nit
    )
VALUES
    (
        uuid_generate_v4 (),
        '075540271936',
        TRUE,
        '1240368974',
        'davivienda@gmail.com',
        TRUE,
        'DAVIVIENDA EL SALVADOR',
        '06142711011120'
    ),
    (
        uuid_generate_v4 (),
        '075540271937',
        TRUE,
        '1240368978',
        'aeroman@gmail.com',
        TRUE,
        'AEROMAN S.A. de C.V.',
        '06142711011121'
    ),
    (
        uuid_generate_v4 (),
        '075540271938',
        TRUE,
        '1240368979',
        'tigo@gmail.com',
        TRUE,
        'TIGO S.A. de C.V.',
        '06142711011131'
    );

ALTER TABLE entities ADD CONSTRAINT entities_pkey PRIMARY KEY (id);

INSERT INTO
    public.roles (role_id, role_name, role_description)
VALUES
    (
        uuid_generate_v4 (),
        'MANAGER',
        'Role with full system permissions'
    ),
    (
        uuid_generate_v4 (),
        'SUPPLIER',
        'Role for suppliers to manage their data'
    ),
    (
        uuid_generate_v4 (),
        'AUTHORIZING',
        'Role for authorizing standard operations'
    ),
    (
        uuid_generate_v4 (),
        'AUTHORIZING_TWO_MODE_AUTH',
        'Role for authorizing with two‑mode authentication'
    ),
    (
        uuid_generate_v4 (),
        'SUPPLIER_TWO_MODE_AUTH',
        'Role for suppliers with two‑mode authentication'
    ),
    (
        uuid_generate_v4 (),
        'OPERATOR',
        'Role for system operators with limited permissions'
    );

-- MANAGER
INSERT INTO
    public.users (id, dui, email, name, entity_id, role_role_id)
VALUES
    (
        uuid_generate_v4 (),
        '063062817',
        'mario@ejemplo.com',
        'Mario Antonio Perez Quintanilla',
        '4e8e0daa-4f85-4fea-8932-3aa0db33b7f1',
        '68057681-0e24-418a-b949-b22c22946010'
    );

-- AUTHORIZING
INSERT INTO
    public.users (id, dui, email, name, entity_id, role_role_id)
VALUES
    (
        uuid_generate_v4 (),
        '063062811',
        'carlos@ejemplo.com',
        'Carlos Alberto Hernández Guerra',
        '5c3bbe6e-a2da-4022-aa1d-353a83a12d62',
        'd35b50c6-f1bc-423d-8fe2-10a9427c9131'
    );

-- AUTHORIZING_TWO_MODE_AUTH
INSERT INTO
    public.users (id, dui, email, name, entity_id, role_role_id)
VALUES
    (
        uuid_generate_v4 (),
        '063062812',
        'paola@ejemplo.com',
        'Paola Maricela Linares López',
        'fa43e067-7d54-4607-9a99-1edbcf394157',
        'ff0857ee-3059-4959-8a74-e7cce63ddcde'
    );

-- SUPPLIER
INSERT INTO
    public.users (id, dui, email, name, entity_id, role_role_id)
VALUES
    (
        uuid_generate_v4 (),
        '063062813',
        'jose@ejemplo.com',
        'José Walberto Guerra González',
        '27779d8e-1987-43b1-9682-7614761032a5',
        '257274d4-8636-4e18-9cf6-53cc3feb6948'
    );

-- SUPPLIER_TWO_MODE_AUTH
INSERT INTO
    public.users (id, dui, email, name, entity_id, role_role_id)
VALUES
    (
        uuid_generate_v4 (),
        '063062814',
        'roberto@ejemplo.com',
        'Roberto Daniel Cerrato Bulnes',
        'f6629adb-bf45-4143-846c-f77e78e39310',
        '65e1e637-8074-4908-91b1-506bff972a4e'
    );

-- OPERATOR
INSERT INTO
    public.users (id, dui, email, name, entity_id, role_role_id)
VALUES
    (
        uuid_generate_v4 (),
        '06306285',
        'marvin@ejemplo.com',
        'Marvin Antonio Parada Fuentes',
        'cea5646d-7203-401f-a354-60f639f1a244',
        '28ec8c43-76f2-4f28-b3cc-c2ff7ad87bb9'
    );


INSERT INTO
    public.app_parameters  (id, param_key, param_value)
VALUES
    (
        uuid_generate_v4 (),
        'mailjet.api.key',
        'edf84877873814982a24fa4374133f98'
    ),
	(
		uuid_generate_v4 (),
        'mailjet.api.secret',
        '6e7f71610a399601e9f639c4f4b0a377'
	),
	(
		uuid_generate_v4 (),
        'mailjet.email.sender',
        'chernandez-27@outlook.com'
	),
		(
		uuid_generate_v4 (),
        'mailjet.email.manager.bank',
        'carlos.a.hernandez@davivienda.com.sv'
	);

INSERT INTO
    public.app_parameters  (id, param_key, param_value)
VALUES
	(
		uuid_generate_v4 (),
        'mailjet.email.manager.bank',
        'carlos.a.hernandez@davivienda.com.sv'
	);

INSERT INTO
    public.app_parameters  (id, param_key, param_value)
VALUES
	(
		uuid_generate_v4 (),
        'mailjet.email.sender.subject',
        'Notificaciones automáticas'
	);



INSERT INTO
    public.app_parameters  (id, param_key, param_value)
VALUES
	(
		uuid_generate_v4 (),
        'param.key.interest',
        '0.18'
	),
	(
		uuid_generate_v4 (),
        'param.key.comission',
        '0.0025'
	),
		(
		uuid_generate_v4 (),
        'param.key.base',
        '360'
	);