package top.weiyuexin.mapper;

import top.weiyuexin.entity.Articlecomment;
//@Mapper  //扫描DAO接口到spring容器
public interface ArticlecommentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Articlecomment record);

    int insertSelective(Articlecomment record);

    Articlecomment selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Articlecomment record);

    int updateByPrimaryKey(Articlecomment record);
}