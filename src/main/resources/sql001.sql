create table plugin_record (
	id serial primary key,
	file_name text not null,
	md5 text not null,
	main_class_name text not null,
	version_class text not null,
	plugin_sdk_version text not null,
	uuid text not null
);

create table plugin_parameter_record (
	id serial primary key,
	name text not null,
	description text not null,
	type text not null,
	secret boolean not null default false,
	required boolean not null default false,
	plugin_id bigint not null references plugin_record(id)
);

create table plugin_execution_plan (
	id serial primary key,
	plan text not null,
	plugin_id bigint not null references plugin_record(id)
);

create table plugin_base_parameter_record (
	id serial primary key,
	name text not null,
	value text null,
	description text not null,
	type text not null,
	secret boolean not null default false,
	required boolean not null default false,
	plugin_id bigint not null references plugin_record(id)
);

create table configuration_storage_s3 (
	id serial primary key,
	s3_service_endpoint text not null,
	s3_region text not null,
	s3_access_key text not null,
	s3_secret_key text not null,
	s3_bucket_name text not null,
	base_prefix text null
);

create table plugin_execution (
	id serial primary key,
	created_at timestamp(6) not null default current_timestamp,
	started_at timestamp(6) null,
	finished_at timestamp(6) null,
	code NUMERIC(4) null,
	success boolean null,
	message text null,
	uuid text not null,
	plugin_id bigint not null references plugin_record(id)
);

create table plugin_execution_log (
	id serial primary key,
	message text not null,
	plugin_execution_id bigint not null references plugin_execution(id)
);

create table plugin_execution_parameter (
	id serial primary key,
	name text not null,
	value text not null,
	type text not null,
	plugin_execution_id bigint not null references plugin_execution(id),
	plugin_parameter_id bigint not null references plugin_parameter_record(id)
);