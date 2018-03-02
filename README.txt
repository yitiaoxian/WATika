:q


2018年2月24日14:57:09
打包git两个提交版本中的差异文件
使用cygwin在git仓库下面的
1.git diff %a% %b% --name-only | xargs tar -czvf gengxinbao.tar.gz
a b为版本号
2.git diff %a% %b% --name-only | xargs zip gengxinbao.zip 



2018年3月2日23:16:19
测试push到github