# ofd的修改日志

## 1. 4月11日晚bug修改记录

### 1.1 bug说明

解析ofd文件时，程序卡死

### 1.2 bug原因

按顺序解析文本内容的xml文件时读取出错

### 1.3 解决方案

修改读取xml文件的逻辑，先解析document.xml中的文本内容的xml文件信息，按照这一信息的顺序进行解析。

### 1.4 结果

能够正常解析ofd，bug解决

### 1.5 记录时间

2018年4月12日09:33:28

## 2. ofd嵌套图片无法抽取

### 2.1 bug说明

ofd的嵌套的图片无法抽取

### 2.2 原因

嵌套流修改之后造成zip的文件嵌套都是file型，处理时未经过嵌套处理的流程

### 2.3 解决方案

在File型处理的时候，增加对于资源文件的识别并加以处理

### 2.4 结果

ofd的嵌套资源文件正常抽取，bug解决

### 2.5 记录时间

2018年4月12日10:53:13