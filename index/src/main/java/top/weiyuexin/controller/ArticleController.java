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

    //写文章页面
    @GetMapping("/article/write")
    public String writePage(){
        return "article/write";
    }

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
            java.text.SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd");
            String s= DateUtil.now();
            Date date =  formatter.parse(s);
            article.setTime(date);
            r.setFlag(articleService.save(article));
            r.setMsg("文章发表成功!");
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
    @DeleteMapping("article/{id}")
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

    @PutMapping("article")
    @ResponseBody
    public Object update(Article article){
        R r = new R();

        return r;
    }

    /**
     * 根据id查询单篇文章
     * @param id
     * @return
     */
    @GetMapping("article/{id}")
    @ResponseBody
    public ModelAndView getById(@PathVariable("id") Integer id){
        ModelAndView modelAndView = new ModelAndView();
        //根据id查询文章
        Article article = articleService.getById(id);
        //根据作者id查询作者信息
        User user = userService.getById(article.getAuthorId());
        //格式化时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = time.format(article.getTime());

        modelAndView.setViewName("article/article");
        modelAndView.addObject("article",article);
        modelAndView.addObject("author",user);
        modelAndView.addObject("date",date);

        return modelAndView;
    }

    /**
     * 分页查询接口
     * @param currentPage
     * @param pageSize
     * @param article
     * @return
     */
    @GetMapping("article/{currentPage}/{pageSize}")
    @ResponseBody
    public Object getPage(@PathVariable("currentPage") Integer currentPage,
                          @PathVariable("pageSize") Integer pageSize,
                          Article article){
        IPage<Article> page = articleService.getPage(currentPage,pageSize,article);
        //如果当前页码值大于当前页码值，那么重新执行查询操作，使用最大页码值作为当前页码值
        if(currentPage>page.getPages()){
            page = articleService.getPage(currentPage,pageSize,article);
        }
        //过滤html标签
        List<Article> articles = page.getRecords();
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
        }
        page.setRecords(articles);
        return new R(true,page);
    }
    /**
     * 查询热门文章
     * @param num
     * @return
     */
    @GetMapping("/article/topArticle/{num}")
    @ResponseBody
    public R getTopArticle(@PathVariable("num") Integer num){
        R r = new R();
        List<Article> articles = articleService.getTopArticle(num);
        //过滤html标签
        for(int i=0;i<articles.size();i++){
            String content = articles.get(i).getContent();
            OutHtml outHtml = new OutHtml();
            content = outHtml.delHTMLTag(content);
            if(content.length()>20){
                content=content.substring(0,20);
            }
            articles.get(i).setContent(content);
            //根据作者id查询作者
            User user = userService.getById(articles.get(i).getAuthorId());
            if(user!=null){
                System.out.println(user.getUsername());
                articles.get(i).setAuthorName(user.getUsername());
            }
        }
        if(articles!=null){
            r.setFlag(true);
            r.setData(articles);
            r.setMsg("查询成功");
        }else {
            r.setFlag(false);
            r.setMsg("查询失败，请稍后重试!");
        }
        return r;
    }

    /**
     * 根据类型查询文章
     * @param currentPage
     * @param pageSize
     * @param type
     * @param article
     * @return
     */
    @GetMapping("article/{type}/{currentPage}/{pageSize}")
    @ResponseBody
    public Object getPageByType(@PathVariable("currentPage") Integer currentPage,
                          @PathVariable("pageSize") Integer pageSize,
                          @PathVariable("type") String type,
                          Article article){
        IPage<Article> page = articleService.getPageByType(currentPage,pageSize,type,article);
        //如果当前页码值大于当前页码值，那么重新执行查询操作，使用最大页码值作为当前页码值
        if(currentPage>page.getPages()){
            page = articleService.getPageByType(currentPage,pageSize,type,article);
        }
        //过滤html标签
        List<Article> articles = page.getRecords();
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
        }
        page.setRecords(articles);
        return new R(true,page);
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
            java.text.SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd");
            String s= DateUtil.now();
            Date date =  formatter.parse(s);
            articleComment.setTime(date);
            r.setFlag(articleCommentService.save(articleComment));
            //设置评论数加一
            Article article = articleService.getById(articleComment.getArticleId());
            article.setCommentNum(article.getCommentNum()+1);
            articleService.updateById(article);

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
        for(int i=0;i<articleComments.size();i++){
            authors.add(userService.getById(articleComments.get(i).getAuthorId()));
        }
        Map<String,Object> map = new HashMap<>();
        map.put("comments",articleComments);
        map.put("authors",authors);
        return new R(true,map);
    }

    /**
     * 点赞文章
     * @param article
     * @param session
     * @return
     */
    @PostMapping("/article/star")
    @ResponseBody
    public R starArticle(Article article,HttpSession session){
        R r = new R();
        if((User)session.getAttribute("user")!=null){
            //点赞数加一
            article=articleService.getById(article.getId());
            article.setStar(article.getStar()+1);
            //保存
            r.setFlag(articleService.updateById(article));
            r.setMsg("点赞成功!");
        }else {
            r.setFlag(false);
            r.setMsg("请登录后再来点赞!");
        }
        return r;
    }


}
