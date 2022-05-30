package top.weiyuexin.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import top.weiyuexin.entity.Article;
import top.weiyuexin.entity.ArticleComment;
import top.weiyuexin.entity.User;
import top.weiyuexin.entity.vo.R;
import top.weiyuexin.entity.vo.W;
import top.weiyuexin.service.ArticleCommentService;
import top.weiyuexin.service.ArticleService;
import top.weiyuexin.service.UserService;
import top.weiyuexin.utils.OutHtml;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ArticleController {
    //注入服务
    @Autowired
    private ArticleService articleService;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleCommentService articleCommentService;

    /**
     * 保存文章接口
     * @param article
     * @return
     * @throws ParseException
     */
    @PostMapping("/article/write.do")
    @ResponseBody
    public R write(Article article,HttpSession session) throws ParseException {
        R r = new R();
        User user = (User) session.getAttribute("user");
        //判断用户是否登录
        if(user!=null){
            //设置作者的id
            article.setAuthorId(user.getId());
            //设置发布时间
            SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
            String s= DateUtil.now();
            Date date =  formatter.parse(s);
            article.setTime(date);
            r.setFlag(articleService.save(article));
            r.setMsg("文章发表成功!");
            //发表成功，积分加5
            user.setPoints(user.getPoints()+5);
            userService.updateById(user);
        }else{
            r.setFlag(false);
            r.setMsg("当前未登录，请先前往登录!");
        }

        return r;
    }

    /**
     * 删除文章接口
     * @param id
     * @param session
     * @return
     */
    @DeleteMapping("/article/del/{id}")
    @ResponseBody
    public Object delete(@PathVariable("id") Integer id, HttpSession session){
        R r = new R();
        //判断用户是否登录
        if(session.getAttribute("user")!=null){
            r.setFlag(articleService.removeById(id));
            r.setMsg("文章删除成功");
        }else {
            r.setData("当前登录状态丢失，请重新登录后再试!");
        }
        return r;
    }

    /**
     * 编辑修改文章页面
     * @param id
     * @return
     */
    @GetMapping("/article/edit/{id}")
    public ModelAndView updatePage(@PathVariable("id") Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Article article = articleService.getById(id);
        modelAndView.setViewName("article/editArticle");
        modelAndView.addObject("article",article);
        return modelAndView;
    }

    /**
     * uodate article by id
     * @param article
     * @return
     */
    @PutMapping("/article/edit.do")
    @ResponseBody
    public R update(Article article){
        R r = new R();
        r.setFlag(articleService.updateById(article));
        if(r.getFlag()){
            r.setMsg("文章修改成功!");
        }else {
            r.setMsg("文章修改失败，请稍后重试!");
        }
        return r;
    }


    /**
     * 分页查询接口
     * @param page
     * @param limit
     * @param article
     * @return
     */
    @GetMapping("/article")
    @ResponseBody
    public W getPage(@RequestParam("page") Integer page,
                     @RequestParam("limit") Integer limit,
                     Article article){

        System.out.println(article.toString());
        IPage<Article> Ipage = articleService.getPage(page,limit,article);
        //如果当前页码值大于当前页码值，那么重新执行查询操作，使用最大页码值作为当前页码值
        if(page>Ipage.getPages()){
            Ipage = articleService.getPage(page,limit,article);
        }
        //过滤html标签
        List<Article> articles = Ipage.getRecords();
        for(int i=0;i<articles.size();i++){
            String content = articles.get(i).getContent();
            OutHtml outHtml = new OutHtml();
            content = outHtml.delHTMLTag(content);
            if(content.length()>160){
                content=content.substring(0,160);
            }
            articles.get(i).setContent(content);
            //根据作者id查询作者
            User user = userService.getById(articles.get(i).getAuthorId());
            if(user!=null){
                System.out.println(user.getUsername());
                articles.get(i).setAuthorName(user.getUsername());
            }
            //修改时间格式
            //格式化时间
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(articles.get(i).getTime());
            articles.get(i).setDate(date);
        }
        Ipage.setRecords(articles);
        return new W(0,(int)Ipage.getTotal(),Ipage.getRecords());
    }


    /**
     * 评论文章
     * @param articleComment
     * @param session
     * @return
     */
    @PostMapping("/article/comment/add")
    @ResponseBody
    public R addComment(ArticleComment articleComment,HttpSession session) throws ParseException {
        R r = new R();
        User user = (User) session.getAttribute("user");
        if(user!=null){
            articleComment.setAuthorId(user.getId());
            //设置发布时间
            SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
            String s= DateUtil.now();
            Date date =  formatter.parse(s);
            articleComment.setTime(date);
            r.setFlag(articleCommentService.save(articleComment));
            //设置评论数加一
            Article article = articleService.getById(articleComment.getArticleId());
            article.setCommentNum(article.getCommentNum()+1);
            articleService.updateById(article);

            //发表成功，积分加2
            user.setPoints(user.getPoints()+2);
            userService.updateById(user);

            if(r.getFlag()){
                r.setMsg("评论成功!");
            }else {
                r.setMsg("评论失败，请稍后重试!");
            }
        }else {
            r.setFlag(false);
            r.setMsg("请登录后再来评论");
        }
        return r;
    }

    /**
     * 查询评论
     * @param articleId
     * @return
     */
    @GetMapping("/article/comment/{articleId}")
    @ResponseBody
    public R getComments(@PathVariable("articleId") Integer articleId){
        List<ArticleComment> articleComments = articleCommentService.getCommentById(articleId);
        List<User> authors = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        for(int i=0;i<articleComments.size();i++){
            authors.add(userService.getById(articleComments.get(i).getAuthorId()));
            //格式化时间
            SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dates.add((String)time.format(articleComments.get(i).getTime()));
        }

        Map<String,Object> map = new HashMap<>();
        map.put("comments",articleComments);
        map.put("authors",authors);
        map.put("dates",dates);
        return new R(true,map);
    }

    /**
     * 分页查询文章评论
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/article/comments")
    @ResponseBody
    public W getPageComment(@RequestParam("page") Integer page,
                     @RequestParam("limit") Integer limit){

        IPage<ArticleComment> Ipage = articleCommentService.getPage(page,limit);
        //如果当前页码值大于当前页码值，那么重新执行查询操作，使用最大页码值作为当前页码值
        if(page>Ipage.getPages()){
            Ipage = articleCommentService.getPage(page,limit);
        }
        //过滤html标签
        List<ArticleComment> articleComments = Ipage.getRecords();
        for(int i=0;i<articleComments.size();i++){

            //根据作者id查询作者
            User user = userService.getById(articleComments.get(i).getAuthorId());
            if(user!=null){
                System.out.println(user.getUsername());
                articleComments.get(i).setAuthorName(user.getUsername());
            }
            //修改时间格式
            //格式化时间
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(articleComments.get(i).getTime());
            articleComments.get(i).setDate(date);
        }
        Ipage.setRecords(articleComments);
        return new W(0,(int)Ipage.getTotal(),Ipage.getRecords());
    }

    /**
     * 删除评论
     * @param id
     * @param session
     * @return
     */
    @DeleteMapping("/article/comment/del/{id}")
    @ResponseBody
    public Object deleteComment(@PathVariable("id") Integer id, HttpSession session){
        R r = new R();
        //判断用户是否登录
        if(session.getAttribute("user")!=null){
            r.setFlag(articleCommentService.removeById(id));
            r.setMsg("评论删除成功");
        }else {
            r.setData("当前登录状态丢失，请重新登录后再试!");
        }
        return r;
    }

}
