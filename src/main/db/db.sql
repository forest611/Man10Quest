create table player_quest_data
(
	id int auto_increment,
	player varchar(16) null,
	uuid varchar(36) null,
	quest_name varchar(32) null,
	status varchar(16) null comment 'クエストのステータス
lock:ロック、まだクエストを始められない
unlock:アンロック、クエストができる
clear:クリア
',
	constraint player_quest_data_pk
		primary key (id)
);

create index player_quest_data_uuid_quest_name_index
	on player_quest_data (uuid, quest_name);

