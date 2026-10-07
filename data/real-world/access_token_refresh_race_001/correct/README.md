## WxJava - 微信开发 Java SDK（开发工具包） [![LICENSE](https://img.shields.io/badge/License-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE) [![Badge](https://img.shields.io/badge/Link-996.icu-red.svg)](https://996.icu/#/zh_CN) [![Badge](https://img.shields.io/badge/Link-京东内购福利-red.svg)](https://mp.weixin.qq.com/s/dfwatgMgARaBjh421Todyg)

[![码云Gitee](https://gitee.com/binary/weixin-java-tools/badge/star.svg?theme=blue)](https://gitee.com/binary/weixin-java-tools)
[![Github](http://github-svg-buttons.herokuapp.com/star.svg?user=Wechat-Group&repo=WxJava&style=flat&background=1081C1)](https://github.com/Wechat-Group/WxJava)
[![GitHub release](https://img.shields.io/github/release/Wechat-Group/WxJava.svg)](https://github.com/Wechat-Group/WxJava/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.binarywang/wx-java.svg)](http://mvnrepository.com/artifact/com.github.binarywang/wx-java)
[![Build Status](https://travis-ci.org/Wechat-Group/WxJava.svg?branch=develop)](https://travis-ci.org/Wechat-Group/WxJava)
[![使用IntelliJ IDEA开发维护](https://img.shields.io/badge/IntelliJ%20IDEA-提供支持-blue.svg)](https://www.jetbrains.com/?from=WxJava-weixin-java-tools)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

#### 支持包括微信支付、开放平台、公众号、企业微信/企业号、小程序等微信功能的后端开发。

<table border="0">
	<tbody>
		<tr>
			<td align="left" valign="middle">
        <a href="http://mp.weixin.qq.com/mp/homepage?__biz=MzI3MzAwMzk4OA==&hid=1&sn=f31af3bf562b116b061c9ab4edf70b61&scene=18#wechat_redirect" target="_blank">
				  <img height="120" src="https://gitee.com/binary/weixin-java-tools/raw/master/images/qrcodes/mp.png">
        </a>
			</td>
			<td align="center" valign="middle">
				<a href="https://cloud.tencent.com/redirect.php?redirect=1014&cps_key=a4c06ffe004dbcda44036daa1bf8f876&from=console" target="_blank">
					<img height="120" src="https://gitee.com/binary/weixin-java-tools/raw/master/images/banners/tcloud.jpg">
				</a>
			</td>
			<td align="right" valign="middle">
				<a href="https://mp.weixin.qq.com/s/R30CNEpkELJg4SRkX0mTDA" target="_blank">
					<img height="120" src="https://github.com/Wechat-Group/WxJava/blob/master/images/banners/planB.jpg?raw=true">
				</a>
			</td>
			<td align="center" valign="middle">
				<a href="https://www.vultr.com/?ref=7888900-4F" target="_blank">
					<img height="120" src="https://github.com/Wechat-Group/WxJava/blob/master/images/banners/vultr.jpg?raw=true">
				</a>
			</td>
			<td align="center" valign="middle">
				<a href="https://promotion.aliyun.com/ntms/act/qwbk.html?userCode=7makzf5h" target="_blank">
					<img height="120" src="https://gitee.com/binary/weixin-java-tools/raw/master/images/banners/aliyun.jpg">
				</a>
			</td>
		</tr>
	</tbody>
</table>

### 重要信息
1. **2019-8-10 发布 [【3.5.0正式版】](https://github.com/Wechat-Group/WxJava/releases)**！
1. 新手重要提示：本项目仅是一个SDK开发工具包，未提供Web实现，建议使用 `maven` 或 `gradle` 引用本项目即可使用本SDK提供的各种功能，详情可参考 **[【Demo项目】](demo.md)** 或本项目中的部分单元测试代码；另外微信开发新手请务必阅读[【开发文档 Wiki 首页】](https://github.com/Wechat-Group/WxJava/wiki)的常见问题部分，可以少走很多弯路，节省不少时间。
1. 技术交流群：想获得QQ群/微信群/钉钉企业群等信息的同学，请使用微信扫描上面的微信公众号二维码关注 `WxJava` 后点击相关菜单即可获取加入方式，同时也可以在微信中搜索 `weixin-java-tools` 或 `WxJava` 后选择正确的公众号进行关注，该公众号会及时通知SDK相关更新信息，并不定期分享微信Java开发相关技术知识；
1. 付费QQ群：（**注意：刚入群会有5分钟禁言，稍等片刻即可正常发言**） [![加入QQ群](https://img.shields.io/badge/QQ群-343954419-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=731dc3e7ea31ebe25376cc1a791445468612c63fd0e9e05399b088ec81fd9e15) 或 [![加入QQ群](https://img.shields.io/badge/QQ群-343954419-blue.svg)](http://jq.qq.com/?_wv=1027&k=40lRskK)，或者请自行搜索群号`343954419`进行添加；当然由于某种原因无法入群的，可关注公众号后获取其他群的加入方式；
1. 钉钉企业群：[请点击链接申请加入](https://h5.dingtalk.com/inviteColleague/index.html#/invite/9ed100cc4a/E1DF918E32E398D191E7FE61FE0552A6) 或者 [用手机钉钉APP扫码](https://gitee.com/binary/weixin-java-tools/raw/master/images/qrcodes/ding.jpg) 申请加入。
1. 微信开发新手或者Java开发新手在群内提问或新开Issue提问前，请先阅读[【提问的智慧】](https://github.com/ryanhanwu/How-To-Ask-Questions-The-Smart-Way/blob/master/README-zh_CN.md)，并确保已查阅过 [【开发文档Wiki】](https://github.com/wechat-group/WxJava/wiki) ，避免浪费大家的宝贵时间；
1. 寻求帮助时需贴代码或大长串异常信息的，请利用 http://paste.ubuntu.com 

--------------------------------
### 其他说明
1. **阅读源码的同学请注意，本SDK为简化代码编译时加入了`lombok`支持，如果不了解`lombok`的话，请先学习下相关知识，比如可以阅读[此文章](https://mp.weixin.qq.com/s/cUc-bUcprycADfNepnSwZQ)；**
1. 如有新功能需求，发现BUG，或者由于微信官方接口调整导致的代码问题，可以直接在[【Issues】](https://github.com/Wechat-Group/WxJava/issues)页提出issue，便于讨论追踪问题；
1. 如果需要贡献代码，请务必在提交PR之前先仔细阅读[【代码贡献指南】](CONTRIBUTING.md)，谢谢理解配合；
1. 本SDK要求的最低JDK版本是1.7，还在使用JDK6的用户请参考[【此项目】]( https://github.com/binarywang/weixin-java-tools-for-jdk6) ，而其他更早的JDK版本则需要自己改造实现。
1. [开源中国本项目的首页](https://www.oschina.net/p/weixin-java-tools-new)，欢迎大家积极留言评分 🙂
1. SDK开发文档请查阅 [【开发文档Wiki】](https://github.com/wechat-group/WxJava/wiki)，部分文档可能未能及时更新，如有发现，可以及时上报或者自行修改。
1. **如果本开发工具包对您有所帮助，欢迎对我们的努力进行肯定，可以直接前往[【托管于码云的项目首页】](http://gitee.com/binary/weixin-java-tools)，在页尾部分找到“捐助”按钮进行打赏，多多益善 😄。非常感谢各位打赏和捐助的同学！**
1. 各个模块的Javadoc可以在线查看：[weixin-java-miniapp](http://binary.ac.cn/weixin-java-miniapp-javadoc/)、[weixin-java-pay](http://binary.ac.cn/weixin-java-pay-javadoc/)、[weixin-java-mp](http://binary.ac.cn/weixin-java-mp-javadoc/)、[weixin-java-common](http://binary.ac.cn/weixin-java-common-javadoc/)、[weixin-java-cp](http://binary.ac.cn/weixin-java-cp-javadoc/)、[weixin-java-open](http://binary.ac.cn/weixin-java-open-javadoc/)
1. 本SDK项目在以下代码托管网站同步更新:
* 码云：https://gitee.com/binary/weixin-java-tools
* GitHub：https://github.com/wechat-group/WxJava

---------------------------------
### Maven 引用方式
注意：最新版本（包括测试版）为 [![Maven Central](https://img.shields.io/maven-central/v/com.github.binarywang/wx-java.svg)](http://mvnrepository.com/artifact/com.github.binarywang/wx-java)，以下为最新正式版。

```xml
<dependency>
  <groupId>com.github.binarywang</groupId>
  <artifactId>（不同模块参考下文）</artifactId>
  <version>3.5.0</version>
</dependency>
```

  - 微信小程序：`weixin-java-miniapp`   
  - 微信支付：`weixin-java-pay`
  - 微信开放平台：`weixin-java-open`   
  - 公众号（包括订阅号和服务号）：`weixin-java-mp`    
  - 企业号/企业微信：`weixin-java-cp`


---------------------------------
### 版本说明

<details>
<summary>点此展开查看</summary>
  
1. 本项目定为大约每两个月发布一次正式版（同时 `develop` 分支代码合并进入 `master` 分支），版本号格式为 `X.X.0`（如`2.1.0`，`2.2.0`等），遇到重大问题需修复会及时提交新版本，欢迎大家随时提交Pull Request；
1. BUG修复和新特性一般会先发布成小版本作为临时测试版本（如`3.1.8.B`，即尾号不为0，并添加B，以区别于正式版），代码仅存在于 `develop` 分支中；
1. 目前最新版本号为 [![Maven Central](https://img.shields.io/maven-central/v/com.github.binarywang/wx-java.svg)](http://mvnrepository.com/artifact/com.github.binarywang/wx-java) ，也可以通过访问链接 [【微信支付】](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.binarywang%22%20AND%20a%3A%22weixin-java-pay%22) 、[【微信小程序】](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.binarywang%22%20AND%20a%3A%22weixin-java-miniapp%22) 、[【公众号】](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.binarywang%22%20AND%20a%3A%22weixin-java-mp%22) 、[【企业微信】](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.binarywang%22%20AND%20a%3A%22weixin-java-cp%22)、[【开放平台】](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.binarywang%22%20AND%20a%3A%22weixin-java-open%22)
分别查看所有最新的版本。 

</details>

----------------------------------
### 使用案例
完整案例登记列表，请[【访问这里】](https://github.com/Wechat-Group/weixin-java-tools/issues/729)查看，欢迎登记更多的案例。

以下为部分案例列表：

#### 开源项目：
- 基于微信公众号的签到、抽奖、发送弹幕程序：https://github.com/workcheng/weiya
- XxPay聚合支付：https://github.com/jmdhappy/xxpay-master
- 微同商城：https://gitee.com/fuyang_lipengjun/platform
- 微信点餐系统：https://github.com/sqmax/springboot-project
- 专注批量推送的小而美的工具：https://github.com/rememberber/WePush

#### 小程序：
- （京东）友家铺子，友家铺子店长版，京粉精选
- [喵星人贴吧助手(扫码关注)](http://p98ahz3tg.bkt.clouddn.com/miniappqrcode.jpg)
- 树懒揽书+
- 广廉快线，鹏城巴士等
- 当燃挑战、sportlight轻灵运动
- 360考试宝典
- 民医台（可自行搜索）
- 来一团商家版

#### 公众号：
- 中国电信上海网厅（sh_189）
- E答平台
- 宁夏生鲜365
- 通服货滴
- 神龙养车
- 沃音乐商务智能
- 光环云社群
- 手机排队
- [全民约跑健身便利店](http://www.oneminsport.com/)
- [洽洽食品](https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=gQFM8TwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAycDRPOXBZbVZib2UxMDAwME0wN2gAAgRIu4RbAwQAAAAA)、[洽洽合伙人](https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=gQFP8jwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyOUpJaU5VcXBlWTAxMDAwME0wN1oAAgSau4RbAwQAAAAA)
- 民医台（可自行搜索）

#### 企业号/企业微信：
- 洽洽企业号
- HTC企业微信

#### 其他：
- 高善人力资源
- [小猪餐餐](http://www.xzcancan.com/)
- [餐饮系统](http://canyin.daydao.com)
- 微信公众号管理系统：http://demo.joolun.com
- 锐捷网络：Saleslink

----------------------------------
### 贡献者列表
特别感谢参与贡献的所有同学，所有贡献者列表请在[此处](https://github.com/Wechat-Group/WxJava/graphs/contributors)查看，欢迎大家继续踊跃贡献代码！
<details>
<summary>点击此处展开查看贡献次数最多的几位同学</summary>

1. [chanjarster (Daniel Qian)](http://github.com/chanjarster)
1. [binarywang (Binary Wang)](http://github.com/binarywang)
1. [mgcnrx11](http://github.com/mgcnrx11)
1. [007gzs](http://github.com/007gzs)
1. [aimilin6688 (Jonk)](http://github.com/aimilin6688)
1. [kakotor](http://github.com/kakotor)
1. [kareanyi (MillerLin)](http://github.com/kareanyi)
1. [tianmu](http://github.com/tianmu)
1. [rememberber (周波)](http://github.com/rememberber)
1. [charmingoh (Charming)](http://github.com/charmingoh)

</details>

### GitHub Stargazers over time

[![Stargazers over time](https://starchart.cc/Wechat-Group/WxJava.svg)](https://starchart.cc/Wechat-Group/WxJava)     
