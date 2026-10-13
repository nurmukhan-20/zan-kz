package kz.zankz.server.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Article;
import kz.zankz.server.model.Law;
import kz.zankz.server.repository.ArticleRepository;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final LawService lawService;

    public ArticleService(ArticleRepository articleRepository, LawService lawService) {
        this.articleRepository = articleRepository;
        this.lawService = lawService;
    }

    public List<Article> getAll(Long lawId, String section) {
        if (lawId != null && section != null && !section.isBlank()) {
            return articleRepository.findByLawIdAndSectionOrderByIdAsc(lawId, section.trim());
        }
        if (lawId != null) {
            return articleRepository.findByLawIdOrderByIdAsc(lawId);
        }
        return articleRepository.findAll();
    }

    public List<Article> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return articleRepository.search(query.trim());
    }

    public Article getById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Статья с номером " + id + " не найдена"));
    }

    public Article create(Article article) {
        article.setId(null); // номер назначает база данных
        article.setLaw(findLaw(article));
        return articleRepository.save(article);
    }

    public Article update(Long id, Article data) {
        Article article = getById(id);
        article.setArticleNumber(data.getArticleNumber());
        article.setSection(data.getSection());
        article.setTitleKk(data.getTitleKk());
        article.setTitleRu(data.getTitleRu());
        article.setTitleEn(data.getTitleEn());
        article.setTextKk(data.getTextKk());
        article.setTextRu(data.getTextRu());
        article.setTextEn(data.getTextEn());
        article.setExplanationKk(data.getExplanationKk());
        article.setExplanationRu(data.getExplanationRu());
        article.setExplanationEn(data.getExplanationEn());
        article.setLaw(findLaw(data));
        return articleRepository.save(article);
    }

    public void delete(Long id) {
        Article article = getById(id);
        articleRepository.delete(article);
    }

    private Law findLaw(Article article) {
        if (article.getLaw() == null || article.getLaw().getId() == null) {
            throw new ResourceNotFoundException("Не указан номер закона");
        }
        return lawService.getById(article.getLaw().getId());
    }
}