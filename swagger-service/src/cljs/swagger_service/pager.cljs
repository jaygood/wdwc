(ns swagger-service.pager)


(defn forward [i pages]
      (if (< i (dec pages)) (inc i) i))

(defn back [i]
      (if (pos? i) (dec i) i))

(defn nav-link [page i]
      [:li.page-item>a.page-link.btn.btn-primary
       {:on-click #(reset! page i)
        :class (when (= i @page) "active")}
       [:span i]])

(defn pager [pages page]
      (when (> pages 1)
            (into
              [:div.text-xs-center>ul.pagination.pagination-lg]
              (concat
                [[:li.page-item>a.page-link.btn
                  {:on-click #(swap! page back)
                   :class (when (= @page 0) "disabled")}
                  [:span "<<"]]]
                (map (partial nav-link page) (range pages))
                [[:li.page-item>a.page-link.btn
                  {:on-click #(swap! page forward pages)
                   :class (when (= @page (dec pages)) "disabled")}
                  [:span ">>"]]]))))