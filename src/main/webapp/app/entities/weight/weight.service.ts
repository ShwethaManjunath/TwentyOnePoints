import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { SERVER_API_URL } from '../../app.constants';

import { JhiDateUtils } from 'ng-jhipster';

import { Weight } from './weight.model';
import { ResponseWrapper, createRequestOption } from '../../shared';

@Injectable()
export class WeightService {

    private resourceUrl = SERVER_API_URL + 'api/weights';
    private resourceSearchUrl = SERVER_API_URL + 'api/_search/weights';

    constructor(private http: Http, private dateUtils: JhiDateUtils) { }

    create(weight: Weight): Observable<Weight> {
        const copy = this.convert(weight);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    update(weight: Weight): Observable<Weight> {
        const copy = this.convert(weight);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    find(id: number): Observable<Weight> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            const jsonResponse = res.json();
            return this.convertItemFromServer(jsonResponse);
        });
    }

    query(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
            .map((res: Response) => this.convertResponse(res));
    }

    delete(id: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${id}`);
    }

    search(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceSearchUrl, options)
            .map((res: any) => this.convertResponse(res));
    }

    private convertResponse(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        const result = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            result.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return new ResponseWrapper(res.headers, result, res.status);
    }

    /**
     * Convert a returned JSON object to Weight.
     */
    private convertItemFromServer(json: any): Weight {
        const entity: Weight = Object.assign(new Weight(), json);
        entity.timestamp = this.dateUtils
            .convertLocalDateFromServer(json.timestamp);
        return entity;
    }

    /**
     * Convert a Weight to a JSON which can be sent to the server.
     */
    private convert(weight: Weight): Weight {
        const copy: Weight = Object.assign({}, weight);
        copy.timestamp = this.dateUtils
            .convertLocalDateToServer(weight.timestamp);
        return copy;
    }
}
