USE [smslib]
GO
/****** Object:  Table [dbo].[smssvr_in]    Script Date: 08/12/2007 22:34:16 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[smssvr_in](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[process] [int] NOT NULL,
	[originator] [nvarchar](16) COLLATE Greek_CI_AS NULL,
	[type] [char](1) COLLATE Greek_CI_AS NOT NULL,
	[encoding] [char](1) COLLATE Greek_CI_AS NOT NULL,
	[message_date] [datetime] NULL,
	[receive_date] [datetime] NOT NULL,
	[text] [nvarchar](1000) COLLATE Greek_CI_AS NOT NULL,
	[gateway_id] [nvarchar](64) COLLATE SQL_Latin1_General_CP1253_CI_AI NOT NULL,
 CONSTRAINT [PK_smssvr_in] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF