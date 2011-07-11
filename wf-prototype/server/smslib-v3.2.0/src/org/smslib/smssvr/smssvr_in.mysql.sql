CREATE TABLE `smssvr_in` (
  `id` bigint(20) NOT NULL auto_increment,
  `process` int(11) default NULL,
  `originator` varchar(16) default NULL,
  `type` char(1) default NULL,
  `encoding` char(1) default NULL,
  `message_date` datetime default NULL,
  `receive_date` datetime default NULL,
  `text` varchar(1000) default NULL,
  `gateway_id` varchar(64) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
