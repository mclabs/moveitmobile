CREATE TABLE `smssvr_out` (
  `id` bigint(20) NOT NULL auto_increment,
  `recipient` varchar(16) default NULL,
  `text` varchar(1000) default NULL,
  `create_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `originator` varchar(16) default NULL,
  `encoding` char(1) default '7',
  `status_report` int(11) default '0',
  `flash_sms` int(11) default '0',
  `src_port` int(11) default '-1',
  `dst_port` int(11) default '-1',
  `sent_date` datetime default NULL,
  `ref_no` varchar(64) default NULL,
  `priority` enum('H','N','L') default 'N',
  `errors` int(11) NOT NULL default '0',
  `gateway_id` varchar(64) NOT NULL default '*',
  `status` char(1) default 'U',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
