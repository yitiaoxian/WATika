:q


2018年2月24日14:57:09
打包git两个提交版本中的差异文件
使用cygwin在git仓库下面的
git diff %a% %b% --name-only | xargs tar -czvf gengxinbao.tar.gz