USE [smslib]
GO
/****** Object:  Table [dbo].[smssvr_out]    Script Date: 08/26/2007 00:29:56 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[smssvr_out](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[recipient] [nvarchar](16) COLLATE SQL_Latin1_General_CP1253_CI_AI NOT NULL,
	[text] [nvarchar](1000) COLLATE SQL_Latin1_General_CP1253_CI_AI NOT NULL,
	[create_date] [datetime] NULL CONSTRAINT [DF_smssvr_out_create_date]  DEFAULT (getdate()),
	[originator] [nvarchar](16) COLLATE SQL_Latin1_General_CP1253_CI_AI NULL CONSTRAINT [DF_smssvr_out_from]  DEFAULT (NULL),
	[encoding] [char](1) COLLATE SQL_Latin1_General_CP1253_CI_AI NOT NULL CONSTRAINT [DF_smssvr_out_encoding]  DEFAULT ('7'),
	[status_report] [int] NOT NULL CONSTRAINT [DF_smssvr_out_status_report]  DEFAULT ((0)),
	[flash_sms] [int] NOT NULL CONSTRAINT [DF_smssvr_out_flash_sms]  DEFAULT ((0)),
	[src_port] [int] NOT NULL CONSTRAINT [DF_smssvr_out_src_port]  DEFAULT ((-1)),
	[dst_port] [int] NOT NULL CONSTRAINT [DF_smssvr_out_dst_port]  DEFAULT ((-1)),
	[sent_date] [datetime] NULL CONSTRAINT [DF_smssvr_out_dispatch_date]  DEFAULT (NULL),
	[ref_no] [nvarchar](64) COLLATE SQL_Latin1_General_CP1253_CI_AI NULL CONSTRAINT [DF_smssvr_out_ref_no]  DEFAULT (NULL),
	[priority] [char](1) COLLATE SQL_Latin1_General_CP1253_CI_AI NOT NULL CONSTRAINT [DF_smssvr_out_priority]  DEFAULT ('N'),
	[status] [char](1) COLLATE SQL_Latin1_General_CP1253_CI_AI NOT NULL CONSTRAINT [DF_smssvr_out_status]  DEFAULT ('U'),
	[errors] [int] NOT NULL CONSTRAINT [DF_smssvr_out_errors]  DEFAULT ((0)),
	[gateway_id] [nvarchar](64) COLLATE SQL_Latin1_General_CP1253_CI_AI NOT NULL CONSTRAINT [DF_smssvr_out_gateway_id]  DEFAULT ('*'),
 CONSTRAINT [PK_smssvr_out] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF