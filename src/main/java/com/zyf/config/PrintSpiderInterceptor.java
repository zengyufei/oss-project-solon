package com.zyf.config;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;

import java.io.*;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * 利用字典树捉互联网爬虫
 *
 * @author zyf
 * @date 2024/05/16
 */
@Slf4j
@Component(index = -2)
public class PrintSpiderInterceptor implements RouterInterceptor {

    private static final List<String> igloneList = new ArrayList<>();
    private static final Trie trie = new Trie("poetry", "TRANSLATION");

    static {
        igloneList.add("Googlebot"); // Google 爬虫
        igloneList.add("Baiduspider"); // 百度爬虫
        igloneList.add("Yahoo! Slurp"); // 雅虎爬虫
        igloneList.add("YodaoBot"); // 有道爬虫
        igloneList.add("msnbot"); // Bing爬虫
        igloneList.add("TencentTraveler");
        igloneList.add("Baiduspider+");
        igloneList.add("BaiduGame");
        igloneList.add("Googlebot");
        igloneList.add("msnbot");
        igloneList.add("Sosospider+");
        igloneList.add("Sogou web spider");
        igloneList.add("ia_archiver");
        igloneList.add("Yahoo! Slurp");
        igloneList.add("YoudaoBot");
        igloneList.add("Yahoo Slurp");
        igloneList.add("MSNBot");
        igloneList.add("Java (Often spam bot)");
        igloneList.add("BaiDuSpider");
        igloneList.add("Voila");
        igloneList.add("Yandex bot");
        igloneList.add("BSpider");
        igloneList.add("twiceler");
        igloneList.add("Sogou Spider");
        igloneList.add("Speedy Spider");
        igloneList.add("Google AdSense");
        igloneList.add("Heritrix");
        igloneList.add("Python-urllib");
        igloneList.add("Alexa (IA Archiver)");
        igloneList.add("Ask");
        igloneList.add("Exabot");
        igloneList.add("Custo");
        igloneList.add("OutfoxBot/YodaoBot");
        igloneList.add("yacy");
        igloneList.add("SurveyBot");
        igloneList.add("legs");
        igloneList.add("lwp-trivial");
        igloneList.add("Nutch");
        igloneList.add("StackRambler");
        igloneList.add("The web archive (IA Archiver)");
        igloneList.add("Perl tool");
        igloneList.add("MJ12bot");
        igloneList.add("Netcraft");
        igloneList.add("MSIECrawler");
        igloneList.add("WGet tools");
        igloneList.add("larbin");
        igloneList.add("Fish search");

        for (String pattern : igloneList) {
            trie.addKeywords(pattern);
        }
    }

    /**
     * 拦截处理（包围式拦截） //和过滤器的 doFilter 类似，且只对路由器范围内的处理有效
     */
    @Override
    public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("PrintSpiderInterceptor");
        }
        final String header = ctx.header("User-Agent");
        if (trie.findFirstIgnoreCase(header) != null) {
            final String realIp = ctx.realIp();
            String sourcePath = ctx.uri().getPath();
            log.info("{} 爬虫 {} 当前请求url: {}", header, realIp, sourcePath);
            return;
        }
        chain.doIntercept(ctx, mainHandler);
    }

    /**
     * 提交结果（ render 执行前调用）//不要做太复杂的事情
     */
    @Override
    public Object postResult(Context ctx, Object result) throws Throwable {
        return result;
    }


    /**
     * 字典匹配树
     *
     * @author zyf
     * @date 2024/05/16
     */
    public static class Trie implements Serializable {
        private static final long serialVersionUID = 7464998650081881647L;
        private final State root;

        public Trie() {
            this.root = new State(0);
        }

        public Trie(Set<String> keywords) {
            this.root = new State(0);
            this.addKeywords(keywords);
        }

        public Trie(String... keywords) {
            this.root = new State(0);
            this.addKeywords(keywords);
        }

        public Trie(InputStream src) {
            this.root = new State(0);
            this.addKeywords(src);
        }

        public Trie addKeywords(Set<String> keywords) {
            for (String keyword : keywords) {
                if (keyword != null && !keyword.isEmpty()) {
                    root.addState(keyword).addKeyword(keyword);
                }
            }
            Queue<State> states = new LinkedList<>();
            root.getSuccess().forEach((ignored, state) -> {
                state.setFailure(root);
                states.add(state);
            });
            while (!states.isEmpty()) {
                State state = states.poll();
                state.getSuccess().forEach((c, next) -> {
                    State f = state.getFailure();
                    State fn = f.nextState(c);
                    while (fn == null) {
                        f = f.getFailure();
                        fn = f.nextState(c);
                    }
                    next.setFailure(fn);
                    next.addKeywords(fn.getKeywords());
                    states.add(next);
                });
            }
            return this;
        }

        public Trie addKeywords(String... keywords) {
            if (keywords == null || keywords.length == 0) {
                return this;
            }
            Set<String> keywordSet = new HashSet<>();
            Collections.addAll(keywordSet, keywords);
            return addKeywords(keywordSet);
        }

        public Trie addKeywords(InputStream src) {
            Set<String> keywords = new HashSet<>();
            try (InputStreamReader inputStreamReader = new InputStreamReader(src);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    keywords.add(line);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
            return addKeywords(keywords);
        }

        public Emits findAll(CharSequence text, boolean ignoreCase) {
            Emits emits = new Emits(text);
            State state = root;
            for (int i = 0, len = text.length(); i < len; i++) {
                state = nextState(state, text.charAt(i), ignoreCase);
                for (String keyword : state.getKeywords()) {
                    emits.add(new Emit(i - keyword.length() + 1, i + 1, keyword));
                }
            }
            return emits;
        }

        public Emits findAll(CharSequence text) {
            return findAll(text, false);
        }

        public Emits findAllIgnoreCase(CharSequence text) {
            return findAll(text, true);
        }

        public Emit findFirst(CharSequence text, boolean ignoreCase) {
            State state = root;
            for (int i = 0, len = text.length(); i < len; i++) {
                state = nextState(state, text.charAt(i), ignoreCase);
                String keyword = state.getFirstKeyword();
                if (keyword != null) {
                    return new Emit(i - keyword.length() + 1, i + 1, keyword);
                }
            }
            return null;
        }

        public Emit findFirst(CharSequence text) {
            return findFirst(text, false);
        }

        public Emit findFirstIgnoreCase(CharSequence text) {
            return findFirst(text, true);
        }

        private State nextState(State state, char c, boolean ignoreCase) {
            State next = state.nextState(c, ignoreCase);
            while (next == null) {
                state = state.getFailure();
                next = state.nextState(c, ignoreCase);
            }
            return next;
        }
    }


    /**
     * @author Leego Yih
     */
    public static class Emit implements Serializable {
        private static final long serialVersionUID = -8879895979621579720L;
        /**
         * The beginning index, inclusive.
         */
        private final int begin;
        /**
         * The ending index, exclusive.
         */
        private final int end;
        private final String keyword;

        public Emit(int begin, int end, String keyword) {
            this.begin = begin;
            this.end = end;
            this.keyword = keyword;
        }

        public int getBegin() {
            return begin;
        }

        public int getEnd() {
            return end;
        }

        public int getLength() {
            return end - begin;
        }

        public String getKeyword() {
            return keyword;
        }

        public boolean overlaps(Emit o) {
            return this.begin < o.end && this.end > o.begin;
        }

        public boolean contains(Emit o) {
            return this.begin <= o.begin && this.end >= o.end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Emit)) {
                return false;
            }
            Emit that = (Emit) o;
            return this.begin == that.begin
                    && this.end == that.end
                    && Objects.equals(this.keyword, that.keyword);
        }

        @Override
        public int hashCode() {
            return Objects.hash(begin, end, keyword);
        }

        @Override
        public String toString() {
            return begin + ":" + end + "=" + keyword;
        }
    }


    /**
     * @author Leego Yih
     */
    public static class Emits extends ArrayList<Emit> {
        private static final long serialVersionUID = -9117361135147927914L;
        private final CharSequence source;

        public Emits(CharSequence source) {
            this.source = source;
        }

        private Emits(Emits emits) {
            super(emits);
            this.source = emits.source;
        }

        public CharSequence getSource() {
            return source;
        }

        public List<Token> tokenize() {
            Emits emits = this.copy();
            emits.removeContains();
            String source = emits.getSource().toString();
            List<Token> tokens = new ArrayList<>(emits.size() * 2 + 1);
            if (emits.isEmpty()) {
                tokens.add(new Token(source, null));
                return tokens;
            }
            int index = 0;
            for (Emit emit : emits) {
                if (index < emit.getBegin()) {
                    tokens.add(new Token(source.substring(index, emit.getBegin()), null));
                }
                tokens.add(new Token(source.substring(emit.getBegin(), emit.getEnd()), emit));
                index = emit.getEnd();
            }
            Emit last = emits.get(emits.size() - 1);
            if (last.getEnd() < source.length()) {
                tokens.add(new Token(source.substring(last.getEnd()), null));
            }
            return tokens;
        }

        public String replaceWith(String replacement) {
            Emits emits = this.copy();
            emits.removeContains();
            String source = emits.getSource().toString();
            if (emits.isEmpty()) {
                return source;
            }
            int index = 0;
            StringBuilder sb = new StringBuilder();
            for (Emit emit : this) {
                if (index < emit.getBegin()) {
                    sb.append(source, index, emit.getBegin());
                    index = emit.getBegin();
                }
                sb.append(mask(replacement, index, emit.getEnd()));
                index = emit.getEnd();
            }
            Emit last = emits.get(emits.size() - 1);
            if (last.getEnd() < source.length()) {
                sb.append(source, last.getEnd(), source.length());
            }
            return sb.toString();
        }

        public void removeOverlaps() {
            removeIf(Emit::overlaps);
        }

        public void removeContains() {
            removeIf(Emit::contains);
        }

        private void removeIf(BiPredicate<Emit, Emit> predicate) {
            if (this.size() <= 1) {
                return;
            }
            this.sort();
            Iterator<Emit> iterator = this.iterator();
            Emit emit = iterator.next();
            while (iterator.hasNext()) {
                Emit next = iterator.next();
                if (predicate.test(emit, next)) {
                    iterator.remove();
                } else {
                    emit = next;
                }
            }
        }

        private void sort() {
            this.sort((a, b) -> {
                if (a.getBegin() != b.getBegin()) {
                    return Integer.compare(a.getBegin(), b.getBegin());
                } else {
                    return Integer.compare(b.getEnd(), a.getEnd());
                }
            });
        }

        private String mask(String replacement, int begin, int end) {
            int count = end - begin;
            int len = replacement != null ? replacement.length() : 0;
            if (len == 0) {
                return repeat("*", count);
            } else if (len == 1) {
                return repeat(replacement, count);
            } else {
                char[] chars = new char[count];
                for (int i = 0; i < count; i++) {
                    chars[i] = replacement.charAt((i + begin) % len);
                }
                return new String(chars);
            }
        }

        private String repeat(String s, int count) {
            if (count < 0) {
                throw new IllegalArgumentException("count is negative: " + count);
            }
            if (count == 1) {
                return s;
            }
            final int len = s.length();
            if (len == 0 || count == 0) {
                return "";
            }
            if (Integer.MAX_VALUE / count < len) {
                throw new OutOfMemoryError("Required length exceeds implementation limit");
            }
            if (len == 1) {
                final char[] single = new char[count];
                Arrays.fill(single, s.charAt(0));
                return new String(single);
            }
            final int limit = len * count;
            final char[] multiple = new char[limit];
            System.arraycopy(s.toCharArray(), 0, multiple, 0, len);
            int copied = len;
            for (; copied < limit - copied; copied <<= 1) {
                System.arraycopy(multiple, 0, multiple, copied, copied);
            }
            System.arraycopy(multiple, 0, multiple, copied, limit - copied);
            return new String(multiple);
        }

        private Emits copy() {
            return new Emits(this);
        }
    }


    /**
     * @author Leego Yih
     */
    public static class State implements Serializable {
        private static final long serialVersionUID = -6350361756888572415L;
        private final int depth;
        private Map<Character, State> success;
        private State failure;
        private TreeSet<String> keywords;

        public State(int depth) {
            this.depth = depth;
        }

        public State nextState(char c) {
            return nextState(c, false);
        }

        public State nextState(char c, boolean ignoreCase) {
            State next = getState(c, ignoreCase);
            if (next != null) {
                return next;
            } else if (depth == 0) {
                return this;
            }
            return null;
        }

        public State getState(char c) {
            return success != null ? success.get(c) : null;
        }

        public State getState(char c, boolean ignoreCase) {
            if (success == null) {
                return null;
            }
            State state = success.get(c);
            if (state != null) {
                return state;
            }
            if (ignoreCase) {
                char cc;
                if (Character.isLowerCase(c)) {
                    cc = Character.toUpperCase(c);
                } else if (Character.isUpperCase(c)) {
                    cc = Character.toLowerCase(c);
                } else {
                    cc = c;
                }
                if (c != cc) {
                    return success.get(cc);
                }
            }
            return null;
        }

        public State addState(CharSequence cs) {
            State state = this;
            for (int i = 0; i < cs.length(); i++) {
                state = state.addState(cs.charAt(i));
            }
            return state;
        }

        public State addState(char c) {
            if (success == null) {
                success = new HashMap<>();
            }
            State state = success.get(c);
            if (state == null) {
                state = new State(depth + 1);
                success.put(c, state);
            }
            return state;
        }

        public void addKeyword(String keyword) {
            if (this.keywords == null) {
                this.keywords = new TreeSet<>();
            }
            this.keywords.add(keyword);
        }

        public void addKeywords(Collection<String> keywords) {
            if (this.keywords == null) {
                this.keywords = new TreeSet<>();
            }
            this.keywords.addAll(keywords);
        }

        public Set<String> getKeywords() {
            return keywords != null ? keywords : Collections.emptySet();
        }

        public String getFirstKeyword() {
            return keywords != null && keywords.size() > 0 ? keywords.first() : null;
        }

        public State getFailure() {
            return failure;
        }

        public void setFailure(State failure) {
            this.failure = failure;
        }

        public Map<Character, State> getSuccess() {
            return success != null ? success : Collections.emptyMap();
        }

        public int getDepth() {
            return depth;
        }

        public boolean isRoot() {
            return depth == 0;
        }
    }


    /**
     * @author Leego Yih
     */
    public static class Token implements Serializable {
        private static final long serialVersionUID = -7918430275428907853L;
        private final String fragment;
        private final Emit emit;

        public Token(String fragment, Emit emit) {
            this.fragment = fragment;
            this.emit = emit;
        }

        public String getFragment() {
            return this.fragment;
        }

        public Emit getEmit() {
            return emit;
        }

        public boolean isMatch() {
            return emit != null;
        }

        @Override
        public String toString() {
            if (emit == null) {
                return fragment;
            } else {
                return fragment + "(" + emit + ")";
            }
        }
    }

}
