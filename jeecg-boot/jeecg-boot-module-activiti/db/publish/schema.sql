
create table if not exists pub_app
(
    id               int auto_increment primary key,
    name             varchar(50)  not null,
    package_name      varchar(100) not null,
    short_code        varchar(50)  not null,
    description      varchar(500) null,
    current_version_id int          null,
    ios_url           varchar(500) null,
    create_time       datetime     null,
    update_time       datetime     null,
    constraint app_shortCode_uindex unique (shortCode)
);

create table if not exists pub_app_version
(
    id            int auto_increment
        primary key,
    app_id         int           not null,
    name          varchar(50)   not null,
    version_name   varchar(50)   not null,
    version_code   int           not null,
    size          int           null,
    icon          text          null,
    download_url   varchar(100)  not null,
    download_count int           null,
    change_log     varchar(1000) null,
    create_time    datetime      null
)

